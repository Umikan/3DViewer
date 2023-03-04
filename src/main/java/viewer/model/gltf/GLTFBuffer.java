package viewer.model.gltf;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import static com.jogamp.opengl.GL4.*;

public class GLTFBuffer {
    private List<IntBuffer> buffers = new LinkedList<IntBuffer>();

    private int[] count;
    private int[] indexToComponentType;
    private int[] indexToBufferName;
    public int getBufferName(int index){
        return indexToBufferName[index];
    }
    public int getComponentType(int index){
        return indexToComponentType[index];
    }
    public int getCount(int index){
        return count[index];
    }
    public void release(GL4 gl){
        for (IntBuffer buffer : buffers) gl.glDeleteBuffers(1, buffer);
    }

    public GLTFBuffer(GL4 gl, String parentDir, GLTF gltf){
        List<Byte[]> buffers = createBytesFromFile(parentDir, gltf);
        List<AccessorBin> accessorBytes = createAccessorBytes(buffers, gltf);
        int size = accessorBytes.size();
        indexToBufferName = new int[size];
        indexToComponentType = new int[size];
        count = new int[size];
        for (int i = 0; i < size; i++){
            AccessorBin bin = accessorBytes.get(i);
            List<ByteBuffer> bytes = createByteBuffers(bin);
            count[i] = bin.count;
            //TODO: write a code when target is empty
            if (bin.target == GLTF.EMPTY){
                bin.target = automaticTarget(bin.componentType);
            }
            indexToBufferName[i] = createBufferObject(gl, bytes, bin);
            indexToComponentType[i] = bin.componentType;
        }
    }

    //Load binary files.
    public List<Byte[]> createBytesFromFile(String parentDir, GLTF gltf){
        List<Byte[]> buffers = new ArrayList<Byte[]>();
        for (GLTF.Buffer buffer : gltf.buffers){
            String path = parentDir + "/" + buffer.uri;
            try (DataInputStream dis = new DataInputStream(new FileInputStream(path))){
                byte[] data = new byte[buffer.byteLength];
                if (dis.read(data) != buffer.byteLength){
                    throw new IOException("Unexpected End of Stream");
                }
                buffers.add(toBigByte(data));
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't load buffers.");
                System.exit(0);
            }
        }
        return buffers;
    }

    private static final int threadNum = 4;
    private static class BinaryReader implements Callable<Byte[]>{
        private final int byteLength, byteOffset;
        private Byte[] in;
        public BinaryReader(GLTF.BufferView bv, Byte[] in){
            this.byteLength = bv.byteLength;
            this.byteOffset = bv.byteOffset;
            this.in = in;
        }
        public BinaryReader(GLTF.Accessor ac, BufferViewBin bufferView){
            this.byteLength = ac.count * componentTypeSize(ac) * componentNumber(ac);
            this.byteOffset = ac.byteOffset;
            this.in = bufferView.binData;
        }
        @Override
        public Byte[] call() throws Exception {
            Byte[] out = new Byte[byteLength];
            for (int i = 0; i < byteLength; i++){
                out[i] = in[byteOffset + i];
            }
            return out;
        }
    }
    //Create a set of binaries of an accessor.
    private List<BufferViewBin> createBufferViewBytes(List<Byte[]> buffers, GLTF gltf){
        Collection<Callable<Byte[]>> reader
            = new ArrayList<Callable<Byte[]>>();
        ExecutorService threadpool = Executors.newFixedThreadPool(threadNum);

        List<BufferViewBin> bufferViews = new ArrayList<BufferViewBin>();
        for(int i = 0; i < gltf.bufferViews.length; i++){
            GLTF.BufferView bv = gltf.bufferViews[i];
            reader.add(new BinaryReader(bv, buffers.get(bv.buffer)));
        }
        try {
            List<Future<Byte[]>> futures = threadpool.invokeAll(reader);
            threadpool.shutdown();
            for (int i = 0; i < gltf.bufferViews.length ;i++){
                bufferViews.add(new BufferViewBin(futures.get(i).get(), gltf.bufferViews[i]));
            }
        } catch (Exception e){
            e.printStackTrace();
            threadpool.shutdown();
        }
        return bufferViews;
    }
    private List<AccessorBin> createAccessorBytes(List<Byte[]> buffers, GLTF gltf){
        List<BufferViewBin> bufferViews = createBufferViewBytes(buffers, gltf);

        Collection<Callable<Byte[]>> reader 
            = new ArrayList<Callable<Byte[]>>();
        ExecutorService threadpool = Executors.newFixedThreadPool(threadNum);

        List<AccessorBin> accessors = new ArrayList<AccessorBin>();
        for(int i = 0; i < gltf.accessors.length; i++){
            GLTF.Accessor ac = gltf.accessors[i];
            reader.add(new BinaryReader(ac, bufferViews.get(ac.bufferView)));
        }
        try {
            List<Future<Byte[]>> futures = threadpool.invokeAll(reader);
            threadpool.shutdown();
            for (int i = 0; i < gltf.accessors.length ;i++){
                GLTF.Accessor ac = gltf.accessors[i];
                BufferViewBin bin = bufferViews.get(ac.bufferView);
                accessors.add(new AccessorBin(futures.get(i).get(), bin, ac));
            }
        } catch (Exception e){
            e.printStackTrace();
            threadpool.shutdown();
        }
        return accessors;
    }
    /*
    private List<AccessorBin> createAccessorBytes2(List<Byte[]> buffers, GLTF gltf){
        List<BufferViewBin> bufferViews = 
            Arrays.stream(gltf.bufferViews).map((GLTF.BufferView bv) -> {
                Stream<Byte> in = Arrays.stream(buffers.get(bv.buffer)).sequential();
                Byte[] out = in.skip(bv.byteOffset).limit(bv.byteLength).toArray(Byte[]::new);
                return new BufferViewBin(out, bv);
            }).collect(Collectors.toList());

        List<AccessorBin> accessors = 
            Arrays.stream(gltf.accessors).map((GLTF.Accessor ac) -> {
                Stream<Byte> in = Arrays.stream(bufferViews.get(ac.bufferView).binData).sequential();
                Byte[] out = in.skip(ac.byteOffset)
                    .limit(ac.count * componentTypeSize(ac) * componentNumber(ac))
                    .toArray(Byte[]::new);
                return new AccessorBin(out, bufferViews.get(ac.bufferView), ac);
            }).collect(Collectors.toList());

        return accessors;
    }*/

    //Create Buffer List
    private List<ByteBuffer> createByteBuffers(AccessorBin bin){
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();

        int pos = 0;
        final int blankSize = bin.byteStride - bin.componentNumber * bin.componentTypeSize;
        final boolean strideFlag = bin.byteStride != 0;
        for (int i = 0; i < bin.count; i++){
            for (int k = 0; k < bin.componentNumber; k++){
                Byte[] bytes = new Byte[bin.componentTypeSize];
                for (int idx = 0; idx < bin.componentTypeSize; idx++, pos++){
                    bytes[idx] = bin.binData[pos];
                }
                byte[] out = toSmallByte(bytes);
                ByteBuffer byteBuffer = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN);
                byteBuffers.add(byteBuffer);
            }
            if (strideFlag) pos += blankSize;
        }
        return byteBuffers;
    }
    //Create Buffer Object
    public int createBufferObject(GL4 gl, List<ByteBuffer> byteBuffer, AccessorBin bin){
        Buffer buffer = null;
        int byteLength = 0;
        switch(bin.componentType){
            case GL_BYTE: 
            case GL_UNSIGNED_BYTE:
                byte[] byteArray = new byte[byteBuffer.size()];
                for (int i = 0; i < byteBuffer.size(); i++){
                    byteArray[i] = byteBuffer.get(i).get();
                }
                buffer = Buffers.newDirectByteBuffer(byteArray);
                byteLength = buffer.capacity() * Buffers.SIZEOF_BYTE;
                break;
            case GL_SHORT: 
            case GL_UNSIGNED_SHORT:
                short[] shortArray = new short[byteBuffer.size()];
                for (int i = 0; i < byteBuffer.size(); i++){
                    shortArray[i] = byteBuffer.get(i).getShort();
                }
                buffer = Buffers.newDirectShortBuffer(shortArray);
                byteLength = buffer.capacity() * Buffers.SIZEOF_SHORT; 
                break;
            case GL_UNSIGNED_INT:
                int[] intArray = new int[byteBuffer.size()];
                for (int i = 0; i < byteBuffer.size(); i++){
                    intArray[i] = byteBuffer.get(i).getInt();
                }
                buffer = Buffers.newDirectIntBuffer(intArray);
                byteLength = buffer.capacity() * Buffers.SIZEOF_INT; 
                break;
            case GL_FLOAT:
                float[] floatArray = new float[byteBuffer.size()];
                for (int i = 0; i < byteBuffer.size(); i++){
                    floatArray[i] = byteBuffer.get(i).getFloat();
                }
                buffer = Buffers.newDirectFloatBuffer(floatArray);
                byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT; 
                break;
            default: break;
        }
        IntBuffer nameBuffer = IntBuffer.allocate(1);
        buffers.add(nameBuffer);
        gl.glGenBuffers(1, nameBuffer);
        int id = nameBuffer.get(0);
        gl.glBindBuffer(bin.target, id);
        gl.glBufferData(bin.target, byteLength, buffer, GL_STATIC_DRAW);
        return id;
    }

    private Byte[] toBigByte(byte[] source){
        Byte[] result = new Byte[source.length];
        for (int i = 0; i < source.length; i++)
            result[i] = Byte.valueOf(source[i]);
        return result;
    }
    private byte[] toSmallByte(Byte[] source){
        byte[] result = new byte[source.length];
        for (int i = 0; i < source.length; i++)
            result[i] = source[i].byteValue();
        return result;
    }
    public static int componentTypeSize(GLTF.Accessor ac){
        switch (ac.componentType){
            case GL_BYTE: return 1;
            case GL_UNSIGNED_BYTE: return 1;
            case GL_SHORT: return 2;
            case GL_UNSIGNED_SHORT: return 2;
            case GL_UNSIGNED_INT: return 4;
            case GL_FLOAT: return 4;
            default: return -1;
        }
    }
    //TODO: specify which type is which target
    public static int automaticTarget(int componentType){
        switch (componentType){
            case GL_SHORT:
            case GL_UNSIGNED_SHORT:
            case GL_UNSIGNED_INT:
                return GL_ELEMENT_ARRAY_BUFFER;
            case GL_FLOAT: 
            case GL_UNSIGNED_BYTE: 
                return GL_ARRAY_BUFFER;
            default: 
                System.out.println("Invalid target.");
                System.exit(0);
                return -1;
        }  
    }
    public static int componentNumber(GLTF.Accessor ac){
        switch (ac.type){
            case "SCALAR": return 1;
            case "VEC2": return 2;
            case "VEC3": return 3;
            case "VEC4": return 4;
            case "MAT2": return 4;
            case "MAT3": return 9;
            case "MAT4": return 16;
            default: return -1;
        }
    }
    //For Debug
    private float[] byteBufferToFloatArray(List<ByteBuffer> byteBuffer){
        float[] floatArray = new float[byteBuffer.size()];
        for (int i = 0; i < byteBuffer.size(); i++){
            floatArray[i] = byteBuffer.get(i).getFloat();
        }
        return floatArray;
    }
    private int[] byteBufferToIntArray(List<ByteBuffer> byteBuffer){
        int[] intArray = new int[byteBuffer.size()];
        for (int i = 0; i < byteBuffer.size(); i++){
            intArray[i] = byteBuffer.get(i).getShort();
        }
        return intArray;
    }
    private void printBuffer(List<ByteBuffer> byteBuffer, String type){
        float[] array = byteBufferToFloatArray(byteBuffer);
        try (PrintWriter pw = new PrintWriter(new FileWriter("test.txt", true)) ){
            switch(type){
                case "v":
                case "vn":
                for (int i = 0; i < array.length; i+=3){
                    pw.printf("%s %f %f %f\n", type, array[i], array[i+1], array[i+2]);
                }
                    break;
                case "vt":
                for (int i = 0; i < array.length; i+=2){
                    pw.printf("%s %f %f\n", type, array[i], array[i+1]);
                }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void printIndexBuffer(List<ByteBuffer> byteBuffer){
        try (PrintWriter pw = new PrintWriter(new FileWriter("test.txt", true)) ){
            int[] array = byteBufferToIntArray(byteBuffer);
            for (int i = 0; i < array.length; i+=3){
                pw.printf("f %d/%d/%d %d/%d/%d %d/%d/%d\n",
                     array[i]+1, array[i]+1, array[i]+1, 
                     array[i+1]+1, array[i+1]+1, array[i+1]+1, 
                     array[i+2]+1, array[i+2]+1, array[i+2]+1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}