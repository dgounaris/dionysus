package dgounaris.dionysus.view

import kotlinx.html.*

fun postAutoplayView(html: HTML) {
    html.body {
        p {
            +"Playback started successfully"
        }
        br
        p {
            +"For feedback on inaccurate selection please press: "
            form {
                action = "http://localhost:8888/playback/feedback"
                method = FormMethod.post
                input {
                    type = InputType.submit
                    value = "Submit feedback"
                }
            }
        }
    }
}