package qed.sut.apichallenges

data class SimEntities (
    val title : String,
    val doneStatus: Boolean,
    val description : String
)
data class Resp_SimEntities(
    val id : Int,
    val name : String,
    val description: String
)

