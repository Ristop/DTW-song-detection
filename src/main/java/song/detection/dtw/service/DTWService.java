package song.detection.dtw.service;

import javazoom.jl.decoder.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DTWService {


    private static double min(double min1, double min2, double min3) {
        return Math.min(Math.min(min1, min2), min3);
    }

    private static double dist(double a, double b) {
        return Math.abs(a - b);
    }

    private static double distance(double[] song1, double[] song2) {
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

    public UploadResultsDTO analyzeTracks(List<MultipartFile> uploads) {
        // Get original upload as bytes.
        List<byte[]> files = uploads.stream().map(this::uploadToBytes).collect(Collectors.toList());

        // Get frames as doubles.
        List<double[]> audioContents = files.stream()
                .map(this::getAudio)
                .map(array -> array.stream().mapToDouble(Short::doubleValue).toArray())
                .collect(Collectors.toList());


        byte[] track1Raw = files.get(0);
        byte[] track2Raw = files.get(1);

        String dtw = "s";String.valueOf(distance(audioContents.get(0), audioContents.get(1)));

        return new UploadResultsDTO(Collections.singletonList(new ResultDTO(getTitle(track1Raw), getTitle(track2Raw), dtw)));

    }

    private List<Short> getAudio(byte[] raw) {
        List<Short> out = new ArrayList<>();

        try {
            // TODO: how do we know that this will work?
            // Source: https://stackoverflow.com/questions/12099114/decoding-mp3-files-with-jlayer

            // TODO: What should be the condition to end while loop?
            Bitstream bitStream = new Bitstream(new ByteArrayInputStream(raw));
            Header frame;
            while ((frame = bitStream.readFrame()) != null) {

                Decoder decoder = new Decoder();
                SampleBuffer samples = (SampleBuffer) decoder.decodeFrame(frame, bitStream); //returns the next 2304 samples
                bitStream.closeFrame();
                out.addAll(Arrays.asList(ArrayUtils.toObject(samples.getBuffer())));
                // TODO:  More frames will fill up the RAM. RM break later.
                break;
            }

            // No auto-closable support
            bitStream.close();

            return out;
        } catch (BitstreamException | DecoderException e) {
            // TODO: handle
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
