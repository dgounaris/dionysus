package dgounaris.dionysus.view

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.p

fun loginView(html: HTML, authUrl: String) {
    html.body {
        p {
            +"Click this link to login: "
            a(authUrl) { +"Login" }
        }
    }
}