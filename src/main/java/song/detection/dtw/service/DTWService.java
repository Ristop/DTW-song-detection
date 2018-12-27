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

    private int distance(double[] song1, double[] song2) {
        // TODO: https://stackoverflow.com/questions/8138526/getting-mp3-audio-signal-as-array-in-java ANY USE?
        // TODO: implement
        return -1;
    }
}
