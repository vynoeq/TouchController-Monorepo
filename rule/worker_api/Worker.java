package top.fifthlight.bazel.worker.api;

import com.google.devtools.build.lib.worker.ProtoWorkerMessageProcessor;
import com.google.devtools.build.lib.worker.WorkRequestHandler;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Worker {
    private static class WorkRequestCallbackWrapper extends WorkRequestHandler.WorkRequestCallback {
        public WorkRequestCallbackWrapper(Worker worker) {
            super((request, out) -> {
                try {
                    var sandboxDir = request.getSandboxDir();
                    if (!sandboxDir.isEmpty()) {
                        return worker.handleRequest(out, Path.of(request.getSandboxDir()), request.getArgumentsList().toArray(new String[0]));
                    } else {
                        return worker.handleRequest(out, Path.of("."), request.getArgumentsList().toArray(new String[0]));
                    }
                } catch (Exception e) {
                    e.printStackTrace(out);
                    return 1;
                }
            });
        }
    }

    public final void run(String... args) throws Exception {
        var argsList = new ArrayList<>(Arrays.asList(args));
        var index = argsList.indexOf("--persistent_worker");
        if (index == -1) {
            var out = new PrintWriter(System.out);
            var status = handleRequest(out, Path.of("."), args);
            out.flush();
            System.exit(status);
        }
        var handlerBuilder = new WorkRequestHandler.WorkRequestHandlerBuilder(new WorkRequestCallbackWrapper(this), System.err, new ProtoWorkerMessageProcessor(System.in, System.out));
        handlerBuilder.setCpuUsageBeforeGc(Duration.ofSeconds(10));
        handlerBuilder.setIdleTimeBeforeGc(Duration.ofSeconds(30));
        try (var handler = handlerBuilder.build()) {
            handler.processRequests();
        }
    }

    protected abstract int handleRequest(PrintWriter out, Path sandboxDir, String... args) throws Exception;
}
