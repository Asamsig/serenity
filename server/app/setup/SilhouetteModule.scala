package setup

import auth.{DefaultEnv, HospesPasswordHasher, PasswordAuth}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import services.UserIdentityService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]

    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordAuth]
    bind[PasswordHasher]
      .annotatedWithName("bcryptHasher")
      .toInstance(new BCryptPasswordHasher)
    bind[PasswordHasher]
      .annotatedWithName("hospesHasher")
      .toInstance(new HospesPasswordHasher)

    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  @Provides
  def idGenerator(implicit ec: ExecutionContext): IDGenerator =
    new SecureRandomIDGenerator()

  @Provides
  def provideEnvironment(
    implicit userService: UserIdentityService,
      authenticatorService: AuthenticatorService[JWTAuthenticator],
    eventBus: EventBus,
    ec: ExecutionContext
  ): Environment[DefaultEnv] =
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )

  @Provides
  def providePasswordHasherRegistry(
      @Named("bcryptHasher") bcryptHasher: PasswordHasher,
      @Named("hospesHasher") hospesHasher: PasswordHasher
  ): PasswordHasherRegistry = {
    PasswordHasherRegistry(bcryptHasher, Seq(hospesHasher))
  }

  @Provides
  def provideAuthInfoRepository(
    implicit passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    ec: ExecutionContext
  ): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config =
      configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")
    new JcaCrypter(config)
  }

  @Provides
  def provideAuthenticatorService(
    implicit @Named("authenticator-crypter") crypter: Crypter,
      idGenerator: IDGenerator,
      configuration: Configuration,
    clock: Clock,
    ec: ExecutionContext
  ): AuthenticatorService[JWTAuthenticator] = {

    implicit val jwtAuthSettingsReader: ValueReader[JWTAuthenticatorSettings] =
      ValueReader.relative(
        c =>
          JWTAuthenticatorSettings(
            fieldName = c.as[String]("headerName"),
            issuerClaim = c.as[String]("issuerClaim"),
            authenticatorExpiry = c.as[FiniteDuration]("authenticatorExpiry"),
            authenticatorIdleTimeout =
              c.getAs[FiniteDuration]("authenticatorIdleTimeout"),
            sharedSecret = c.as[String]("sharedSecret")
        )
      )
    val config =
      configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)
    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }

}
