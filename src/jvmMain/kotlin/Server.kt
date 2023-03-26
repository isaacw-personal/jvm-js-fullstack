import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq


val client = KMongo.createClient().coroutine
val database = client.getDatabase("jvm-js-fullstack")
val collection = database.getCollection<ShoppingListItem>("ShoppingList")

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 9090
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)
            anyHost()
        }
        install(Compression) {
            gzip()
        }
        routing {
            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }
            static("/") {
                resources("")
            }
            get("/hello") {
                call.respondText("Hello, API!")
            }
            route(ShoppingListItem.path) {
                get {
                    call.respond(collection.find().toList())
                }
                get("/{description}") {
                    val items = collection.find().toList().filter { it.description == call.parameters["description"] }
                    call.respond(items)
                }
                post {
                    val newItem = call.receive<ShoppingListItem>()
                    println(String.format("Method Called: %s \nWith Args: %s", call.toString(), newItem))
                    collection.insertOne(newItem)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    println(String.format("Method Called: %s \nWith Args: %s", call.toString(), call.parameters["id"]))
                    val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
                    println(String.format("Extracted ShoppingListItem: ", id))
                    collection.deleteOne(ShoppingListItem::id eq id)
                    println(String.format("Current Items: %s", collection))
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }.start(wait = true)
}