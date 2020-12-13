package ru.bmstu.lab4;


import akka.actor.AbstractActor;
        import akka.japi.pf.ReceiveBuilder;
        import java.util.HashMap;
        import java.util.Map;
public class StoreActor extends AbstractActor {
    private Map<String, Map<String, String>> store = new HashMap<>();
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(StoreMessage.class, m -> {
                    if (!store.containsKey(m.getPackageId())) {
                        store.put(m.getPackageId(), new HashMap<>());
                    }
                    store.get(m.getPackageId()).put(m.getTestName(), m.getResult());
                })
                .match(GetMessage.class, req -> sender().tell(
                        new StoreType(req.getPackageId(), store.get(req.getPackageId())), self())
                ).build();
    }
}