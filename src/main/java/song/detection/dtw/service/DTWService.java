package song.detection.dtw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import song.detection.dtw.dto.PointDTO;
import song.detection.dtw.dto.ResultDTO;
import song.detection.dtw.dto.TrackDTO;
import song.detection.dtw.dto.VectorDTO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static DTWResult distance(float[] song1, float[] song2) {
        float[][] dtw = new float[song2.length + 1][song1.length + 1];

        // init
        for (int i = 0; i <= song2.length; i++) {
            dtw[i][0] = 10000;
        }

        for (int j = 0; j <= song1.length; j++) {
            dtw[0][j] = 10000;
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

        return new DTWResult(dtw[song2.length][song1.length], getStretchVectors(dtw, song1, song2));
    }


    private static List<VectorDTO> getStretchVectors(float[][] matrix, float[] a, float[] b) {
        int ai = a.length;
        int bi = b.length;
        List<VectorDTO> vectorDTOS = new ArrayList<>();
        while (ai != 0 && bi != 0) {


            PointDTO point1DTO = new PointDTO(bi - 1, b[bi - 1] + 1);
            PointDTO point2DTO = new PointDTO(ai - 1, a[ai - 1]);

            VectorDTO vectorDTO = new VectorDTO("blah", List.of(point1DTO, point2DTO), "#78909C", false);
            vectorDTOS.add(vectorDTO);

            // System.out.println(bi + "\t" + b[bi - 1] + "\t" + ai + "\t" + a[ai - 1]);

            float di = matrix[bi - 1][ai - 1];
            float up = matrix[bi][ai - 1];
            float left = matrix[bi - 1][ai];

            if (di <= up && di <= left) {
                bi--;
                ai--;
            } else if (up <= di && up <= left) {
                ai--;
            } else if (left <= di && left <= di) {
                bi--;
            }

        }
        return vectorDTOS;
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

    public ResultDTO analyzeTracks(List<MultipartFile> uploads) {
        // Get original upload as bytes.
        List<byte[]> files = uploads.stream().map(this::uploadToBytes).collect(Collectors.toList());

        // Get frames as floats.
        List<float[]> audioContents = files.stream()
                .map(this::getAudio)
                .collect(Collectors.toList());


        float[] arr1 = nonNegative(audioContents.get(0));
        float[] arr2 = nonNegative(audioContents.get(1));

        int rate = Integer.max(arr1.length, arr2.length) / 100;
        float[] song1 = reduce(arr1, rate);
        float[] song2 = reduce(arr2, rate);

        normalize(song1);
        normalize(song2);

        DTWResult result = distance(song1, song2);

        for (int i = 0; i < song2.length; i++) {
            song2[i] = song2[i] + 1;
        }

        TrackDTO trackDTO1 = new TrackDTO(uploads.get(0).getOriginalFilename(), song1);
        TrackDTO trackDTO2 = new TrackDTO(uploads.get(1).getOriginalFilename(), song2);
        return new ResultDTO(List.of(trackDTO1, trackDTO2), result.getVectorDTOList(), result.getDistance());

    }

    private float[] nonNegative(float[] arr) {
        return toFloatArray(IntStream.range(0, arr.length)
                .mapToDouble(i -> arr[i]).filter(i -> i > 0).toArray());
    }

    private float[] toFloatArray(double[] arr) {
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float) arr[i];
        }
        return ret;
    }

    private void normalize(float[] original) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for (float v : original) {
            if (max < v) {
                max = v;
            }
            if (min > v) {
                min = v;
            }
        }

        for (int i = 0; i < original.length; i++) {
            original[i] = (original[i] - min) / (max - min);
        }
    }

    private float[] reduce(float[] original, int reduceEvery) {
        int originalLen = original.length;

        float[] reduced = new float[originalLen / reduceEvery];

        int newI = 0;
        for (int i = 0; i < originalLen - reduceEvery; i += reduceEvery) {

            float avg = 0;
            for (int j = 0; j < reduceEvery; j++) {
                avg += original[i + j];
            }

            reduced[newI] = avg / reduceEvery;
            newI++;
        }

        return reduced;
    }

    // https://stackoverflow.com/questions/39736877/sample-frame-in-context-of-audioinputstream
    private float[] getAudio(byte[] raw) {

        try {
            // http://www.javazoom.net/mp3spi/documents.html
            // https://stackoverflow.com/questions/39736877/sample-frame-in-context-of-audioinputstream

            AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(raw));


            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    PCM_FLOAT,
                    baseFormat.getSampleRate(),
                    baseFormat.getSampleSizeInBits(),
                    baseFormat.getChannels(),
                    baseFormat.getFrameSize(),
                    baseFormat.getSampleRate(),
                    baseFormat.isBigEndian()
            );
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

}
