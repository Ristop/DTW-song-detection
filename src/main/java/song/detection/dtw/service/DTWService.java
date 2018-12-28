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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DTWService {


    private static float min(float min1, float min2, float min3) {
        return Math.min(Math.min(min1, min2), min3);
    }

    private static float dist(float a, float b) {
        return Math.abs(a - b);
    }

    private static float distance(float[] song1, float[] song2) {
        float[][] dtw = new float[song2.length + 1][song1.length + 1];

        // init
        for (int i = 0; i <= song2.length; i++) {
            dtw[i][0] = Float.MAX_VALUE;
        }

        for (int j = 0; j <= song1.length; j++) {
            dtw[0][j] = Float.MAX_VALUE;
        }

        dtw[0][0] = 0;

        for (int j = 1; j <= song1.length; j++) {
            for (int i = 1; i <= song2.length; i++) {

                float min1 = dtw[i - 1][j - 1];
                float min2 = dtw[i - 1][j];
                float min3 = dtw[i][j - 1];

                dtw[i][j] = dist(song1[j - 1], song2[i - 1]) + min(min1, min2, min3);
            }
        }

        return dtw[song2.length - 1][song1.length - 1];
    }

    //TODO: cite this part
    private static short[] shortMe(byte[] bytes) {
        short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        for (int i = 0; i < out.length; i++) {
            out[i] = bb.getShort();
        }
        return out;
    }

    //TODO: cite this part
    private static float[] floatMe(short[] pcms) {
        float[] floaters = new float[pcms.length];
        for (int i = 0; i < pcms.length; i++) {
            floaters[i] = pcms[i];
        }
        return floaters;
    }

    public UploadResultsDTO analyzeTracks(List<MultipartFile> uploads) {
        // Get original upload as bytes.
        List<byte[]> files = uploads.stream().map(this::uploadToBytes).collect(Collectors.toList());

        // Get frames as floats.
        List<float[]> audioContents = files.stream()
                .map(this::getAudio)
                .collect(Collectors.toList());


        byte[] track1Raw = files.get(0);
        byte[] track2Raw = files.get(1);

        String dtw = String.valueOf(distance(audioContents.get(0), audioContents.get(1)));

        return new UploadResultsDTO(Collections.singletonList(new ResultDTO(getTitle(track1Raw), getTitle(track2Raw), dtw)));

    }

    // https://stackoverflow.com/questions/39736877/sample-frame-in-context-of-audioinputstream
    private float[] getAudio(byte[] raw) {

        try {
            // http://www.javazoom.net/mp3spi/documents.html
            // https://stackoverflow.com/questions/39736877/sample-frame-in-context-of-audioinputstream

            AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(raw));


            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(PCM_FLOAT,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);


            byte[] bytes = din.readAllBytes();
            din.close();

            return floatMe(shortMe(bytes));
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
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
    private String getTitle(byte[] song) {
        try (InputStream input = new ByteArrayInputStream(song)) {
            Metadata metadata = new Metadata();

            new Mp3Parser().parse(input, new DefaultHandler(), metadata, new ParseContext());
            return metadata.get("title");

        } catch (IOException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
