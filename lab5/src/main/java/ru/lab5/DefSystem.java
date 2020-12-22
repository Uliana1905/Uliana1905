package ru.lab5;

import akka.NotUsed;
import akka.actor.ActorRef;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;


import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DefSystem {

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        ActorRef storeActor = system.actorOf(Props.create(storeActor.class));
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = create(materializer, storeActor);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    public static Flow<HttpRequest, HttpResponse, NotUsed> create (ActorMaterializer materializer, ActorRef storeActor ){
        return Flow.of(HttpRequest.class)
                .map(
                         (msg) -> {
                             Query first_param = msg.getUri().query();
                             String URL = first_param.get("testUrl").get();
                             Integer count = Integer.parseInt(first_param.get("count").get());
                             return new Pair<>(URL, count);
                         }).mapAsync(
                                 1, (Pair<String, Integer> pair) -> {
                                 SentActorMsg newMes = new SentActorMsg(pair.first());
                                 CompletionStage<Object> ans = Patterns.ask(storeActor, newMes, Duration.ofMillis(10);
                                 return ans.thenCompose(
                                         (Object answer) ->{
                                             if ((Integer)answer!=0) {
                                                 return CompletableFuture.completedFuture(new Pair<>(pair.first(),(Integer)answer));
                                             }
                                             Flow<Pair<String, Integer>,Integer, NotUsed> flow = Flow.<Pair<String,Integer>> create()
                                                     .mapConcat(p ->{
                                                         List <String>
                                                     })
                                         })
                        })
    }

}
