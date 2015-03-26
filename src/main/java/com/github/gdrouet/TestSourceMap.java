package com.github.gdrouet;

import com.google.debugging.sourcemap.SourceMapGeneratorV3;
import com.google.debugging.sourcemap.SourceMapParseException;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * <p>
 * This class shows how you can deal with scripts aggregation already minified and don't break their associated sourcemap.
 * The sample relies on clojure compiler. It creates a {@link SourceMapGeneratorV3} and appends to it all the sourcemaps
 * thanks to the {@link SourceMapGeneratorV3#mergeMapSection(int, int, String)} method. The first argument must be the
 * line number where the associated minified script starts in the aggregated file.
 * </p>
 *
 * <p>
 * Both aggregated scripts and sourcemaps are written to the temporary directory.
 * </p>
 */
public class TestSourceMap {

    /**
     * Main.
     *
     * @param args arguments are not read
     * @throws IOException if I/O error occurs
     * @throws SourceMapParseException if a sourcemap is does not match V3 specs
     */
    public static void main(final String ... args) throws IOException, SourceMapParseException {
        final String tmp = System.getProperty("java.io.tmpdir") + "test.js";
        final String[] files = new String[] { "angular", "angular-animate", "angular-aria", "angular-messages" };
        final FileOutputStream aggregate = new FileOutputStream(tmp);
        final SourceMapGeneratorV3 g = new SourceMapGeneratorV3();
        int lineNumber = 0;

        // Append all scripts and associated sourcemaps
        for (final String fileName : files) {
            g.mergeMapSection(lineNumber, 0, IOUtils.toString(TestSourceMap.class.getResourceAsStream("/" + fileName + ".min.js.map")));
            final List<String> lines = IOUtils.readLines(TestSourceMap.class.getResourceAsStream("/" + fileName + ".min.js"));

            for (final String line : lines) {
                // Don't keep sourceMappingURL, we will write a new one ate the end of the file
                if (!line.contains("sourceMappingURL")) {
                    aggregate.write(line.getBytes());
                    aggregate.write("\r\n".getBytes());
                    lineNumber++;
                }
            }

            aggregate.write("\r\n".getBytes());
            lineNumber++;
        }

        aggregate.write("//# sourceMappingURL=test.js.map".getBytes());
        aggregate.close();

        final StringBuilder sb = new StringBuilder();
        g.appendTo(sb, "test.js.map");
        final FileOutputStream os = new FileOutputStream(tmp + ".map");
        IOUtils.copy(new StringReader(sb.toString()), os);
        os.close();

        System.out.println("Aggregated file: " + tmp);
    }
}
