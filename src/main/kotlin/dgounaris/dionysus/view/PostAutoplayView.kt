package dgounaris.dionysus.view

import kotlinx.html.*

fun postAutoplayView(html: HTML) {
    html.body {
        p {
            +"Playback started successfully"
        }
        br
        p {
            form {
                action = "http://localhost:8888/playback/pause"
                method = FormMethod.post
                input {
                    type = InputType.submit
                    value = "Pause"
                }
            }
            br
            form {
                action = "http://localhost:8888/playback/resume"
                method = FormMethod.post
                input {
                    type = InputType.submit
                    value = "Resume"
                }
            }
            br
            form {
                action = "http://localhost:8888/playback/next"
                method = FormMethod.post
                input {
                    type = InputType.submit
                    value = "Next"
                }
            }
            br
            form {
                action = "http://localhost:8888/playback/stop"
                method = FormMethod.post
                input {
                    type = InputType.submit
                    value = "Stop"
                }
            }
        }
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