import groovy.transform.CompileStatic
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

def options = [
  logActivity: true
]

def router = Router.router(vertx)
router.route().handler(BodyHandler.create())

@CompileStatic
class Root implements Handler<RoutingContext> {
  static Root create() {
    println 'Creating instance'
    new Root()
  }

  RoutingContext routingContext

  HttpServerResponse getResponse() {
    this.routingContext.response()
  }

  void handle(RoutingContext context) {
    this.routingContext = context
    response.putHeader('Content-Type', 'text/plain')
    response.end('XXXX')
  }
}

router.get('/').handler(Root.create())

vertx.createHttpServer(options).requestHandler(router.&accept).listen(8080)
