import com.google.inject.AbstractModule
import services.{ Configuration, DefaultConfiguration }

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Configuration]).to(classOf[DefaultConfiguration])
  }

}
