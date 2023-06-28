package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.tryMax;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This sample is based on our official tutorials:
 * <ul>
 *   <li><a href="https://gatling.io/docs/gatling/tutorials/quickstart">Gatling quickstart tutorial</a>
 *   <li><a href="https://gatling.io/docs/gatling/tutorials/advanced">Gatling advanced tutorial</a>
 * </ul>
 */
public class ComputerDatabaseSimulation3 extends Simulation {

    FeederBuilder<String> feeder = csv("search.csv").random();

    ChainBuilder search =
        exec(http("Home").get("/"))
            .pause(1)
            .feed(feeder)
            .exec(
                http("Search")
                    .get("/computers?f=#{searchCriterion}")
                    .check(
                        css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")
                    )
            )
            .pause(1)
            .exec(
                http("Select")
                    .get("#{computerUrl}")
                    .check(status().is(200))
            )
            .pause(1);

    // repeat is a loop resolved at RUNTIME
    ChainBuilder browse =
        // Note how we force the counter name, so we can reuse it
        repeat(4, "i").on(
            exec(
                http("Page #{i}")
                    .get("/computers?p=#{i}")
            ).pause(1)
        );

    HttpProtocolBuilder httpProtocol =
        http.baseUrl("https://computer-database.gatling.io")
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .acceptEncodingHeader("gzip, deflate")
            .userAgentHeader(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0"
            );

    ScenarioBuilder users = scenario("Users").exec(search, browse);

    {
        setUp(
            users.injectOpen(rampUsers(1).during(1))
        ).protocols(httpProtocol);
    }
}
