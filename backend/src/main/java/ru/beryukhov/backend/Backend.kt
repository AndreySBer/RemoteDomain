
package ru.beryukhov.backend

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
@Location("/")
class Index()

@KtorExperimentalLocationsAPI
@Location("/post/{id}")
data class Post(val id: String)

//https://github.com/ktorio/ktor-samples/tree/master/app/youkube
@KtorExperimentalLocationsAPI
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)
    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for Range, Accept-Range and Content-Range headers
    install(PartialContent)


    // Register all the routes available to this application.
    // To allow better scaling for large applications,
    // we have moved those route registrations into several extension methods and files.
    routing {
        videos("DI_SAMPLE")

        styles()
        static {
            // This marks index.html from the 'web' folder in resources as the default file to serve.
            defaultResource("index.html", "web")
            // This serves files from the 'web' folder in the application resources.
            resources("web")
        }
    }
}