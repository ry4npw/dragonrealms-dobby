package pw.ry4n.dr.engine.parser;

public class IndexBuffer {
	public int[] position = null;
	public int[] length = null;
	public byte[] type = null; /* assuming a max of 256 types (1 byte / type) */

	public int count = 0; // the number of tokens / elements in the IndexBuffer.

    public IndexBuffer(int capacity, boolean useTypeArray) {
        this.position = new int[capacity];
        this.length   = new int[capacity];
        if(useTypeArray){
            this.type = new byte[capacity];
        }
    }
}
