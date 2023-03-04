package viewer.model.gltf;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileInputStream;

public class JSON{
    private static final ObjectMapper mapper = new ObjectMapper();
/*
    private static String load(String path){
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
            stream.forEach(line -> { builder.append(line); });
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a glTF file.");
            System.exit(0);
        }
        return builder.toString();
    }
*/
    public static <T> T deserialize(String path, Class<T> type){
        T result = null;
        try (FileInputStream is = new FileInputStream(path)) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            result = mapper.readValue(is, type);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static <T> void print(T obj){
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setSerializationInclusion(Include.NON_NULL);
            String json = mapper.writeValueAsString(obj);
            System.out.println(json);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a glTF file.");
            System.exit(0);
        }
    }
    public static String getParentDirectory(String path){
        return new File(path).getAbsoluteFile().getParent();
    }
}