package qed.testbaseclass

import qed.reports.Logger

class HasRest(val baseTest : BaseTest, var urlKey : String) : IHasRest {

    val logger = baseTest.logger

    private val restClient: ThreadLocal<RestClient> = ThreadLocal.withInitial {
        RestClient(baseTest.url(urlKey), Logger(), baseTest)
    }

    override val rest : RestClient
        get() = restClient.get()

}
