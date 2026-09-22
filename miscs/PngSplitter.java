import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class PngSplitter {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Użycie: java PngSplitter.java <katalog>");
            System.exit(1);
        }

        Path directory = Path.of(args[0]);
        if (!Files.isDirectory(directory)) {
            System.err.println("Nie znaleziono katalogu: " + directory);
            System.exit(1);
        }

        // Lista powstaje przed zapisem, aby nie przetwarzać nowych wycinków.
        List<Path> files;
        try (Stream<Path> paths = Files.list(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".png")
                                && !name.endsWith("_l.png") && !name.endsWith("_r.png");
                    })
                    .sorted()
                    .collect(Collectors.toList());
        }

        int completed = 0;
        int failed = 0;
        for (Path file : files) {
            try {
                BufferedImage source = ImageIO.read(file.toFile());
                if (source == null) {
                    throw new IOException("Nie można odczytać obrazu PNG");
                }
                if (source.getWidth() < 1684 || source.getHeight() < 1056) {
                    throw new IOException("Obraz ma " + source.getWidth() + " × "
                            + source.getHeight() + "; wymagane minimum: 1684 × 1056");
                }

                String name = file.getFileName().toString();
                String base = name.substring(0, name.length() - 4);
                Path left = directory.resolve(base + "_L.png");
                Path right = directory.resolve(base + "_R.png");
                if (Files.exists(left) || Files.exists(right)) {
                    throw new IOException("Plik wynikowy już istnieje — pominięto oba wycinki");
                }

                // Współrzędne od zera; prawy i dolny brzeg są wyłączone.
                save(source.getSubimage(234, 24, 960 - 234, 1056 - 24), left);
                save(source.getSubimage(960, 24, 1684 - 960, 1056 - 24), right);
                completed++;
                System.out.println("Zapisano: " + left.getFileName() + " i " + right.getFileName());
            } catch (IOException | RuntimeException e) {
                failed++;
                System.err.println("Błąd: " + file.getFileName() + ": " + e.getMessage());
            }
        }

        System.out.println("Gotowe. Przetworzono: " + completed + ", pominięto/błędy: " + failed);
        if (failed > 0) System.exit(2);
    }

    private static void save(BufferedImage image, Path destination) throws IOException {
        // CREATE_NEW chroni istniejące pliki przed nadpisaniem.
        try (java.io.OutputStream output = Files.newOutputStream(destination,
                java.nio.file.StandardOpenOption.CREATE_NEW,
                java.nio.file.StandardOpenOption.WRITE)) {
            if (!ImageIO.write(image, "png", output)) {
                throw new IOException("Brak obsługi zapisu PNG");
            }
        }
    }
}
