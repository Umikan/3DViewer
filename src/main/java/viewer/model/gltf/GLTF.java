package viewer.model.gltf;

public class GLTF {
    public final static int EMPTY = -1;

    public static class Info{
        public String name;
        public Object extensions;
        public Object extras;
    }

    //Information of asset.
    public Asset asset;
    public static class Asset extends Info{
        public String version; // required
        public String generator, copyright, minVersion;
    }

    //Scene
    public int scene; //default scene number
    public Scene[] scenes;
    public static class Scene extends Info{
        public int[] nodes;
    }

    //Node Composition
    public Node[] nodes;
    public static class Node extends Info{
        public int[] children;
        //Case 1
        public float[] matrix; 
        //Case 2
        public float[] translation;
        public float[] rotation;
        public float[] scale; 
        public int mesh, camera, skin;
        public float[] weights;
        {
            mesh = camera = skin = EMPTY;
        }
    }

    //Mesh
    public Mesh[] meshes;
    public static class Mesh extends Info{
        public Primitive[] primitives; //required
        public float[] weights;
        public static class Primitive{
            public int mode, indices, material;
            public Attributes attributes; //required;
            public Target[] targets;
            {
                mode = 4; //TRIANGLES
                indices = EMPTY;
                material = EMPTY;
            }
            public static class Attributes{
                public int POSITION, NORMAL, TANGENT;
                public int TEXCOORD_0, TEXCOORD_1;
                public int COLOR_0;
                public int JOINTS_0, WEIGHTS_0;
                //Index of each attribute
                {
                    POSITION = NORMAL = TANGENT = EMPTY;
                    TEXCOORD_0 = TEXCOORD_1 = EMPTY;
                    COLOR_0 = EMPTY;
                    JOINTS_0 = WEIGHTS_0 = EMPTY;
                }
            }
            //morph target
            public static class Target{
                public int POSITION, NORMAL, TANGENT;
                {
                    POSITION = NORMAL = TANGENT = EMPTY;
                }
            }
        }
    }

    //Material
    public Material[] materials;
    public static class Material extends Info{
        public PBRMetallicRoughness pbrMetallicRoughness;
        public static class PBRMetallicRoughness{
            public float[] baseColorFactor = {1.0f, 1.0f, 1.0f, 1.0f};
            public float metallicFactor = 1.0f;
            public float roughnessFactor = 1.0f;
            public Texture baseColorTexture;
            public Texture metallicRoughnessTexture;
        }

        public NormalTexture normalTexture;
        public OcculusionTexture occulusionTexture;
        public Texture emissiveTexture;
        public float[] emissiveFactor = {.0f, .0f, .0f};
        public static class Texture{
            public int index; //required
            public int texCoord;
            {
                texCoord = 0;
            }
        }
        public static class NormalTexture extends Texture{
            public float scale = 1.0f;
        }
        public static class OcculusionTexture extends Texture{
            public float strength;
        }

        public String alphaMode = "OPAQUE";
        public float alphaCutoff = 0.5f;
        public boolean doubleSided = false;
    }

    //Camera
    public Camera[] cameras;
    public static class Camera extends Info{
        public String type; //required
        public Perspective perspective; //Case 1
        public Orthographic orthographic; //Case 2
        public static class Perspective{
            public float yfov, znear; //required;
            public float aspectRatio, zfar; //not required
        }
        public static class Orthographic{
            public float xmag, ymag, zfar, znear; //required
        }
    }

    //Texture
    public Texture[] textures;
    public static class Texture extends Info{
        public int sampler, source;
        {
            sampler = source = EMPTY;
        }
    }
    //Image
    public Image[] images;
    public static class Image extends Info{
        //Case 1
        public String uri;
        //Case 2
        public int bufferView;
        public String mimeType;
    }
    //Sampler
    public Sampler[] samplers;
    public static class Sampler extends Info{
        public int magFilter, minFilter, wrapS, wrapT;
        {
            wrapS = wrapT = 10497; //REPEAT
            magFilter = minFilter = EMPTY;
        }
    }

    //Skin
    public Skin[] skins;
    public static class Skin extends Info{
        public int inverseBindMatrices;
        public int[] joints; //required
        public int skeleton;
        {
            skeleton = EMPTY;
        }
    }

    //Animation
    public Animation[] animations;
    public static class Animation extends Info{
        //required
        public Channel[] channels;
        public Sampler[] samplers;
        public static class Channel{
            //required
            public int sampler;
            public Target target;
            public class Target{
                public int node;
                public String path; //required
            }
        }
        public static class Sampler{
            public int input, output; //required
            public String interpolation = "LINEAR";
        }
    }

    //Binary Data Storage
    public Buffer[] buffers;
    public BufferView[] bufferViews;
    public Accessor[] accessors;
    public static class Buffer extends Info{
        public int byteLength; //required
        public String uri;
    }
    public static class BufferView extends Info{
        public int buffer, byteLength; //required
        public int byteOffset, byteStride;
        public int target;
        {
            byteOffset = 0;
            byteStride = 0;
            target = GLTF.EMPTY;
        }
    }
    public static class Accessor extends Info{
        //required
        public int componentType;
        public int count;
        public String type;

        //not required
        public int bufferView;
        public int byteOffset = 0;
        public boolean normalized = false;
        public String[] min, max;
        public Sparse sparse;
        public static class Sparse{
            //all are requireds
            public int count;
            public Indices indices;
            public Values values;
            public static class Indices{
                //required
                public int bufferView;
                public int componentType;

                //not required
                public int byteOffset = 0;
            }
            public static class Values{
                public int bufferView; //required
                public int byteOffset = 0;
            }
        }
    }
}