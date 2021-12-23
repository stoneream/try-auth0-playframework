import auth.{ UserResolver, UserResolverImpl }
import com.google.inject.AbstractModule
import services.{ Configuration, DefaultConfiguration }

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UserResolver]).to(classOf[UserResolverImpl])

    bind(classOf[Configuration]).to(classOf[DefaultConfiguration])
  }

}
