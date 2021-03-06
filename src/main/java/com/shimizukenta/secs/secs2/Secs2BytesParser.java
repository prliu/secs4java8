package com.shimizukenta.secs.secs2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Secs2BytesParser {
	
	private Secs2BytesParser() {
	}
	
	private static class SingletonHolder {
		private static final Secs2BytesParser inst = new Secs2BytesParser();
	}
	
	public static Secs2BytesParser getInstance() {
		return SingletonHolder.inst;
	}
	
	public Secs2 parse(List<ByteBuffer> buffers) throws Secs2BytesParseException {
		
		final ByteBuffersPack pack = new ByteBuffersPack(buffers);
		
		if ( pack.hasRemaining() ) {
			
			Secs2 ss = stpParse(pack);
			
			if ( pack.hasRemaining() ) {
				throw new Secs2BytesParseException("not reach end buffers");
			}
			
			return ss;
			
		} else {
			
			return new Secs2RawBytes();
		}
	}
	
	private static Secs2 stpParse(ByteBuffersPack pack) throws Secs2BytesParseException {
		
		byte b = pack.get();
		
		Secs2Item s2i = Secs2Item.get(b);
		int lengthBits = b & 0x03;
		int size = 0;
		
		if ( lengthBits == 3 ) {
			
			size =  ((int)(pack.get()) << 16) & 0x00FF0000;
			size |= ((int)(pack.get()) <<  8) & 0x0000FF00;
			size |= ((int)(pack.get())      ) & 0x000000FF;
			
		} else if ( lengthBits == 2 ) {
			
			size =  ((int)(pack.get()) <<  8) & 0x0000FF00;
			size |= ((int)(pack.get())      ) & 0x000000FF;
			
		} else if ( lengthBits == 1 ) {
			
			size =  ((int)(pack.get())      ) & 0x000000FF;
		}
		
		if ( s2i == Secs2Item.LIST ) {
			
			List<Secs2> ll = new ArrayList<>();
			
			for (int i = 0 ; i < size ; ++i) {
				ll.add(stpParse(pack));
			}
			
			return new Secs2List(ll);
			
		} else {
			
			byte[] bs = pack.get(size);
			
			switch ( s2i ) {
			case ASCII: {
				return new Secs2Ascii(bs);
				/* break */
			}
			case BINARY: {
				return new Secs2Binary(bs);
				/* break */
			}
			case BOOLEAN: {
				return new Secs2Boolean(bs);
				/* break */
			}
			case INT1: {
				return new Secs2Int1(bs);
				/* break */
			}
			case INT2: {
				return new Secs2Int2(bs);
				/* break */
			}
			case INT4: {
				return new Secs2Int4(bs);
				/* break */
			}
			case INT8: {
				return new Secs2Int8(bs);
				/* break */
			}
			case UINT1: {
				return new Secs2Uint1(bs);
				/* break */
			}
			case UINT2: {
				return new Secs2Uint2(bs);
				/* break */
			}
			case UINT4: {
				return new Secs2Uint4(bs);
				/* break */
			}
			case UINT8: {
				return new Secs2Uint8(bs);
				/* break */
			}
			case FLOAT4: {
				return new Secs2Float4(bs);
				/* break */
			}
			case FLOAT8: {
				return new Secs2Float8(bs);
				/* break */
			}
			case JIS8: {
				return new Secs2Jis8(bs);
				/* break */
			}
			case UNICODE: {
				return new Secs2Unicode(bs);
				/* break */
			}
			default: {
				throw new Secs2UnsupportedDataFormatException();
			}
			}
		}
	}
	
	private static class ByteBuffersPack {
		
		private final List<ByteBuffer> buffers;
		private int buffersSize;
		private int index;
		
		public ByteBuffersPack(List<ByteBuffer> buffers) {
			this.buffers = new ArrayList<>(buffers);
			this.buffersSize = buffers.size();
			this.index = 0;
		}
		
		public boolean hasRemaining() {
			return buffers.stream().anyMatch(ByteBuffer::hasRemaining);
		}
		
		public byte get() throws Secs2BytesParseException {
			for ( ; index < buffersSize; ++index ) {
				ByteBuffer bf = this.buffers.get(index);
				if ( bf.hasRemaining() ) {
					return bf.get();
				}
			}
			throw new Secs2BytesParseException("reach end buffers");
		}
		
		public byte[] get(int size) throws Secs2BytesParseException {
			
			byte[] bs = new byte[size];
			
			if ( size == 0 ) {
				return bs;
			}
			
			int i = 0;
			
			for ( ; index < this.buffersSize; ++index ) {
				
				ByteBuffer bf = this.buffers.get(index);
				
				int rem = bf.remaining();
				
				if ( rem > 0 ) {
					
					if ( rem >= (size - i) ) {
						
						for ( ; i < size; ++i ) {
							bs[i] = bf.get();
						}
						
						return bs;
						
					} else {
						
						while ( bf.hasRemaining() ) {
							bs[i] = bf.get();
							++i;
						}
					}
				}
			}
			
			throw new Secs2BytesParseException("reach end buffers");
		}
	}
	
}
