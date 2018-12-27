package song.detection.dtw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import song.detection.dtw.dto.ResultDTO;
import song.detection.dtw.dto.UploadResultsDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DTWService {


    public UploadResultsDTO analyzeTracks(List<MultipartFile> uploads) {
        List<byte[]> files = uploads.stream().map(this::uploadToBytes).collect(Collectors.toList());
        return new UploadResultsDTO(files.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }


    private byte[] uploadToBytes(MultipartFile file) {
        // TODO: Do something nice, when somebody tries to break the app.
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // https://stackoverflow.com/questions/1645803/how-to-read-mp3-file-tags
    private ResultDTO convertToDTO(byte[] song) {
        try (InputStream input = new ByteArrayInputStream(song)) {

            Metadata metadata = new Metadata();

            new Mp3Parser().parse(input, new DefaultHandler(), metadata, new ParseContext());

            // TODO: error handling if title not present?
            return new ResultDTO(metadata.get("title"));

        } catch (IOException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private double distance(double[] song1, double[] song2) {
        double[][] dtw = new double[song2.length + 1][song1.length + 1];

        // init
        for (int i = 0; i <= song2.length; i++) {
            dtw[i][0] = Double.MAX_VALUE;
        }

        for (int j = 0; j <= song1.length; j++) {
            dtw[0][j] = Double.MAX_VALUE;
        }

        dtw[0][0] = 0;

        for (int j = 1; j <= song1.length; j++) {
            for (int i = 1; i <= song2.length; i++) {

                double min1 = dtw[i - 1][j - 1];
                double min2 = dtw[i - 1][j];
                double min3 = dtw[i][j - 1];

                dtw[i][j] = dist(song1[j - 1], song2[i - 1]) + min(min1, min2, min3);
            }
        }

        return dtw[song2.length - 1][song1.length - 1];
    }

    private static double min(double min1, double min2, double min3) {
        return Math.min(Math.min(min1, min2), min3);
    }

    private static double dist(double a, double b) {
        return Math.abs(a - b);
    }

}
