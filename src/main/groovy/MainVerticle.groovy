import com.hubrick.vertx.elasticsearch.model.SearchResponse
import groovy.transform.CompileStatic
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import com.hubrick.vertx.elasticsearch.ElasticSearchService
import com.hubrick.vertx.elasticsearch.model.SearchOptions
import groovy.json.JsonOutput

def options = [
  logActivity: true
]

def router = Router.router(vertx)
def eb = vertx.eventBus()
def consumer = eb.consumer('eb')
consumer.handler({ message ->
  println("I have received a message: ${message.body()}")
})
router.route().handler(BodyHandler.create())

@CompileStatic
class Root implements Handler<RoutingContext> {
  static Root create(Vertx vtx) {
    println 'Creating instance'
    new Root(vtx)
  }

  ElasticSearchService elasticSearchService

  Root(Vertx vtx) {
    this.elasticSearchService = ElasticSearchService.createEventBusProxy(vtx, 'eb')
  }

  void search(Handler handler) {
    String query = JsonOutput.toJson([
      query: [
        bool: [
          must: [
            term: [
              available: true
            ]
          ]
        ]
      ],
      sort: [
        [
          _geo_distance: [
            location_center: [
              lat: "50.0000",
              lon: "40.0000"
            ],
            order: "asc",
            unit: "km",
            distance_type: "plane"
          ]
        ]
      ],
      size: 1000
    ])
    SearchOptions searchOptions = new SearchOptions().setQuery(new JsonObject(query))
    this.elasticSearchService.search("locations", searchOptions, handler)
  }

  void handle(RoutingContext context) {
    def response = context.response()
    def closure = { SearchResponse r ->
      response.end(r.getHits().toString())
    }
    response.putHeader('Content-Type', 'application/json')
    search {
      println('xxxx')
      closure.&call
    }
  }
}

router.get('/').handler(Root.create(vertx))

vertx.createHttpServer(options).requestHandler(router.&accept).listen(8080)
