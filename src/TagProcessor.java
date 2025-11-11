import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TagProcessor {

    private final Set<String> stopWords = new TreeSet<>();

    public void loadStopWords(Path stopWordsFile) throws IOException {
        stopWords.clear();
        try (BufferedReader br = Files.newBufferedReader(stopWordsFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) stopWords.add(line);
            }
        }
    }

    public boolean hasStopWords() {
        return !stopWords.isEmpty();
    }

    public Map<String, Integer> extractTags(Path textFile) throws IOException {
        Map<String, Integer> freq = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(textFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                // remove non-letter characters, keep spaces
                line = line.replaceAll("[^a-zA-Z']", " ").toLowerCase();
                String[] parts = line.split("\\s+");
                for (String w : parts) {
                    if (w == null || w.isEmpty()) continue;
                    // strip leading/trailing apostrophes (e.g., 'tis -> tis)
                    w = w.replaceAll("^'+|'+$", "");
                    if (w.length() < 1) continue;
                    if (stopWords.contains(w)) continue;
                    freq.put(w, freq.getOrDefault(w, 0) + 1);
                }
            }
        }
        return freq;
    }

    public List<Map.Entry<String,Integer>> sortByFrequency(Map<String,Integer> map) {
        return map.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<String,Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());
    }

    public void saveTags(Path outFile, List<Map.Entry<String,Integer>> sortedTags) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(outFile)) {
            bw.write(String.format("%-6s %s%n", "Count", "Tag"));
            bw.write("------------------------\n");
            for (Map.Entry<String,Integer> e : sortedTags) {
                bw.write(String.format("%6d %s%n", e.getValue(), e.getKey()));
            }
        }
    }
}
