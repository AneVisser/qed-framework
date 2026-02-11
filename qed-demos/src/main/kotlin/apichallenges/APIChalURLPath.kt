package qed.sut.apichallenges

import qed.testbaseclass.IURLPath
import qed.testbaseclass.PayloadKind
import qed.testbaseclass.PayloadKind.Single
import qed.testbaseclass.RequestType


enum class APIChalURLPath(private val path: String,
                          override val method: RequestType,
                          override val responseKind:PayloadKind?,
                          override val payloadKind: PayloadKind?,
    )
    : IURLPath {
    POST_SIM_ENTITIES("/sim/entities", RequestType.POST, Single(Resp_SimEntities::class), Single(SimEntities::class)),
    GET_SIM_ENTITIES("/sim/entities/{entity}", RequestType.GET, null, null),
    PUT_SIM_ENTITIES("/sim/entities/{entity}", RequestType.PUT, null, Single(SimEntities::class)),
    TODOS("/todos", RequestType.GET, null, null)
    ;

    override val route: String
        get() = this.path


}


