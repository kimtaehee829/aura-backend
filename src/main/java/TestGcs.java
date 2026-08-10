import com.google.cloud.storage.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TestGcs {
    public static void main(String[] args) {

        Storage storage = StorageOptions.getDefaultInstance().getService();

        BlobInfo blobInfo = BlobInfo.newBuilder(
                "aura-media-bucket-2026",   // ✅ 여기!!
                "videos/test.mp4"           // ✅ 폴더 포함 경로
        ).build();

        URL url = storage.signUrl(
                blobInfo,
                10,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT), // 🔥 이거 추가
                Storage.SignUrlOption.withV4Signature()
        );

        System.out.println(url);
    }
}