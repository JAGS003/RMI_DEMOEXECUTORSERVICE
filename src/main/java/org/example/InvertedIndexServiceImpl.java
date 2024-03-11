package org.example;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InvertedIndexServiceImpl extends UnicastRemoteObject implements InvertedIndexService {
    private ExecutorService executorService;

    public InvertedIndexServiceImpl() throws RemoteException {
        super();
        // Initialize the ExecutorService with the number of available processors
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        String text = getFileContents(fileName); // Assume this method returns the file content as String

        String[] lines = text.split("\n");
        Map<String, List<Integer>> index = new ConcurrentHashMap<>();

        List<Future<Void>> futures = new ArrayList<>();
        for (String line : lines) {
            Future<Void> future = executorService.submit(new ProcessLineTask(line, index));
            futures.add(future);
        }

        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        return index;
    }

    private String getFileContents(String fileName) {
        // Implement this method to read file content
        return "This is sample file content.";
    }

    private static class ProcessLineTask implements Callable<Void> {
        private final String line;
        private final Map<String, List<Integer>> index;

        public ProcessLineTask(String line, Map<String, List<Integer>> index) {
            this.line = line;
            this.index = index;
        }

        @Override
        public Void call() {
            String[] words = line.split("\\s+");
            int lineNumber = Arrays.asList(line.split("\n")).indexOf(line) + 1;
            for (String word : words) {
                index.computeIfAbsent(word, k -> new ArrayList<>()).add(lineNumber);
            }
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            InvertedIndexService invertedIndexService = new InvertedIndexServiceImpl();
            LocateRegistry.createRegistry(8099);
            Naming.rebind("//localhost:8099/InvertedIndexService", invertedIndexService);
            System.out.println("InvertedIndexService is running...");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
