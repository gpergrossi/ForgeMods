package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

import org.junit.Test;

import com.gpergrossi.util.io.IStreamHandler;
import com.gpergrossi.util.io.IStreamHandlerFixedSize;
import com.gpergrossi.util.io.MD5Hash;
import com.gpergrossi.util.io.ndmf.NamedDataMapFile;

public class NamedDataMapFileTest {
	
	public static final IStreamHandlerFixedSize<String> NAME_HANDLER = new IStreamHandlerFixedSize<String>() {
		
		public static final int MAX_LENGTH = 6;
		
		public final Writer<String> WRITER = new Writer<String>() {
			@Override
			public void write(OutputStream os, String obj) throws IOException {
				if (obj.length() > MAX_LENGTH) throw new IllegalArgumentException("String is too long!");
				
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeUTF(obj);
			}
		};
		
		public final Reader<String> READER = new Reader<String>() {
			@Override
			public String read(InputStream is) throws IOException {
				DataInputStream dis = new DataInputStream(is);
				return dis.readUTF();
			}
		};
		
		@Override
		public Writer<String> getWriter() {
			return WRITER;
		}
		
		@Override
		public Reader<String> getReader() {
			return READER;
		}
		
		@Override
		public int getMaxSize() {
			return 2+MAX_LENGTH;
		}
	};

	public static final IStreamHandler<BufferedImage> DATA_HANDLER = new IStreamHandler<BufferedImage>() {
		public final Writer<BufferedImage> WRITER = new Writer<BufferedImage>() {
			@Override
			public void write(OutputStream os, BufferedImage obj) throws IOException {
				ImageIO.write(obj, "PNG", os);
			}
		};
		
		public final Reader<BufferedImage> READER = new Reader<BufferedImage>() {
			@Override
			public BufferedImage read(InputStream is) throws IOException {
				return ImageIO.read(is);
			}
		};
		
		@Override
		public Writer<BufferedImage> getWriter() {
			return WRITER;
		}
		
		@Override
		public Reader<BufferedImage> getReader() {
			return READER;
		}
	};
	
	//@Test
	public void testCreation() throws IOException {
		NamedDataMapFile<String, BufferedImage> ndmf = new NamedDataMapFile<>(NAME_HANDLER, DATA_HANDLER, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();
		
		ndmf.open(file);
	}
	
	//@Test
	public void testWriteRead() throws IOException {
		NamedDataMapFile<String, BufferedImage> ndmf = new NamedDataMapFile<>(NAME_HANDLER, DATA_HANDLER, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		ndmf.open(file);
		System.out.println();
		System.out.println("======== WRITING ========");
		for (int i = 1; i < 32; i++) {
			ndmf.debugGetStoredNames().put(":"+i, i+0xF000);
		}
		System.out.println();
		System.out.println("======== READING ========");
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0xF000), ndmf.debugGetStoredNames().get(":"+i));
		}
		ndmf.close();
	}
	
	//@Test
	public void testWriteCloseRead() throws IOException {
		NamedDataMapFile<String, BufferedImage> ndmf = new NamedDataMapFile<>(NAME_HANDLER, DATA_HANDLER, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		System.out.println();
		System.out.println("======== WRITING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			ndmf.debugGetStoredNames().put(":"+i, i+0xF000);
		}
		ndmf.close();
		
		System.out.println();
		System.out.println("======== READING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0xF000), ndmf.debugGetStoredNames().get(":"+i));
		}
		ndmf.close();
	}

	//@Test
	public void testOverwriteCloseRead() throws IOException {
		NamedDataMapFile<String, BufferedImage> ndmf = new NamedDataMapFile<>(NAME_HANDLER, DATA_HANDLER, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		System.out.println();
		System.out.println("======== WRITING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			ndmf.debugGetStoredNames().put(":"+i, i+0x2000);
		}
		
		System.out.println();
		System.out.println("======== OVERWRITING ========");
		for (int i = 1; i < 32; i++) {
			ndmf.debugGetStoredNames().put(":"+i, i+0x1000);
		}
		ndmf.close();
		
		System.out.println();
		System.out.println("======== READING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0x1000), ndmf.debugGetStoredNames().get(":"+i));
		}
		ndmf.close();
	}
	
	public static final long SEED = 1057403912L;
	public static final int BLOCK_SIZE = 4096;

	public static final int NUM_PASSES = 20;
	public static final int NUM_THREADS = 8;
	
	// The consistency check is important, but will not allow
	// proper multithreaded testing if it is enabled
	public static final boolean CHECK_CONSISTENCY = false;
	
	public static final int NUM_IMAGES = 40;
	public static final int MIN_DIMENSION = 20;
	public static final int MAX_DIMENSION = 40;
	
	public static final int NUM_WRITES = 50;
	public static final int NUM_OVERWRITES = 20;
	public static final int NUM_CLEARS = 35;
	
	@Test
	public void testHeavy() {
		try {
		
		NamedDataMapFile<String, BufferedImage> ndmf = new NamedDataMapFile<>(NAME_HANDLER, DATA_HANDLER, BLOCK_SIZE);
		ndmf.debug = true;
		ndmf.debugVerifyOnLoad = true;
		
		File file = new File("E:\\NDMFTest");
		if (file.exists()) file.delete();
		
		final Random random = new Random(SEED);
		
		System.out.println("Building images...");
		final BufferedImage[] images = new BufferedImage[NUM_IMAGES];
		final String[] md5s = new String[NUM_IMAGES];
		for (int i = 0; i < NUM_IMAGES; i++) {
			final int width = random.nextInt(MAX_DIMENSION-MIN_DIMENSION+1)+MIN_DIMENSION;
			final int height = random.nextInt(MAX_DIMENSION-MIN_DIMENSION+1)+MIN_DIMENSION;
			final long seed = random.nextLong();			
			images[i] = createImage(width, height, seed);
			md5s[i] = getImageMD5(images[i]);
			System.out.println("   Image "+(i+1)+"/"+NUM_IMAGES+": [width="+width+", height="+height+" md5="+md5s[i]+"]");
		}
		System.out.println("Done!");
		System.out.println();
		System.out.println();
		System.out.println();
		
		final Map<String, BufferedImage> checkResults = new ConcurrentHashMap<>();
		
		for (int pass = 1; pass <= NUM_PASSES; pass++) {
			System.out.println("===== Pass "+pass+" =====");
			ndmf.open(file);
			
			if (CHECK_CONSISTENCY) {
				System.out.println("Verifying...");
				for (Entry<String, BufferedImage> entry : checkResults.entrySet()) {
					BufferedImage read = ndmf.get(entry.getKey());
					if (read == null || !imageEqual(entry.getValue(), read)) throw new AssertionError("Expected:'"+getImageMD5(entry.getValue())+"' Read:'"+getImageMD5(read)+"'");
				}
				System.out.println("Passed!");
			}

			System.out.println("Simulating Work... ("+NUM_THREADS+" threads)");
			Thread[] threads = new Thread[NUM_THREADS];
			
			ReentrantLock lock = new ReentrantLock(true);
			for (int i = 0; i < NUM_THREADS; i++) {
				long threadSeed = random.nextLong();
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						simulateWork(lock, ndmf, new Random(threadSeed), images, md5s, checkResults);
					}
				});
				thread.setName("Work thread "+i);
				thread.start();
				threads[i] = thread;
			}

			for (int i = 0; i < NUM_THREADS; i++) {
				threads[i].join();
			}
			System.out.println("Done!");
			
			System.out.println("Closing.");
			ndmf.close();

			System.out.println("===== End of Pass "+pass+" =====");
			System.out.println();
			System.out.println();
			System.out.println();
		}
		
		ndmf.open(file);
		System.out.println("Verifying...");
		for (Entry<String, BufferedImage> entry : checkResults.entrySet()) {
			BufferedImage read = ndmf.get(entry.getKey());
			assertTrue(imageEqual(entry.getValue(), read));
		}
		System.out.println("Passed!");
		ndmf.close();
		
		} catch (AssertionError e) {
			System.out.println("FAILED: "+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void simulateWork(ReentrantLock lock, NamedDataMapFile<String, BufferedImage> ndmf, Random random, BufferedImage[] images, String[] md5s, Map<String, BufferedImage> checkResults) {
		final byte OP_WRITE = 1;
		final byte OP_OVERWRITE = 2;
		final byte OP_CLEAR = 3;
		
		final List<Byte> operations = new ArrayList<>(NUM_WRITES + NUM_OVERWRITES + NUM_CLEARS);
		for (int i = 0; i < NUM_WRITES; i++) operations.add(OP_WRITE);
		for (int i = 0; i < NUM_OVERWRITES; i++) operations.add(OP_OVERWRITE);
		for (int i = 0; i < NUM_CLEARS; i++) operations.add(OP_CLEAR);
		Collections.shuffle(operations, random);

		for (Byte operation : operations) {

			if (CHECK_CONSISTENCY) lock.lock();
			
			switch (operation) {
				case OP_WRITE: {
					final String name = randomString(random, 6, false, false, false, false);
					final int imageIndex = random.nextInt(images.length);
					final BufferedImage image = images[imageIndex];
					final String md5 = md5s[imageIndex];

					if (CHECK_CONSISTENCY) {
						checkResults.put(name, image);
						
						System.out.println("   Writing [name="+name+", image='"+md5+"']");
						ndmf.put(name, image);
					} else {
						checkResults.put(name, image);
						ndmf.put(name, image);
					}
						
					break;
				}
					
				case OP_OVERWRITE: {
					final String name = getRandomFromSet(checkResults.keySet(), random);
					if (name == null) break;
					
					final int imageIndex = random.nextInt(images.length);
					final BufferedImage image = images[imageIndex];
					final String md5 = md5s[imageIndex];

					if (CHECK_CONSISTENCY) {
						final BufferedImage old = checkResults.put(name, image);
						final String oldMD5 = getImageMD5(old);
							
						System.out.println("   Overwriting [name="+name+", image='"+oldMD5+"'] with [name="+name+", image="+md5+"]");
						final BufferedImage result = ndmf.put(name, image);

						if (old == null) throw new NullPointerException("Null checkResults");
						if (result == null) throw new NullPointerException("Null result");
						if (!imageEqual(old, result)) throw new AssertionError("Old value mismatch! Expected:'"+oldMD5+"' Result:'"+getImageMD5(result)+"'");
						if (!imageEqual(image, ndmf.get(name))) throw new AssertionError("New value mismatch! Expected:'"+oldMD5+"' Result:'"+getImageMD5(result)+"'");
					} else {
						checkResults.put(name, image);
						ndmf.put(name, image);
					}
					
					break;
				}
				
				case OP_CLEAR: {
					final String name = getRandomFromSet(checkResults.keySet(), random);
					if (name == null) break;

					if (CHECK_CONSISTENCY) {
						final BufferedImage old = checkResults.remove(name);
						final String oldMD5 = getImageMD5(old);
	
						System.out.println("   Removing [name="+name+", image='"+oldMD5+"']");
						final BufferedImage result = ndmf.put(name, null);

						if (old == null) throw new NullPointerException("Null checkResults");
						if (result == null) throw new NullPointerException("Null result");
						if (!imageEqual(old, result)) throw new AssertionError("Expected:'"+oldMD5+"' Result:'"+getImageMD5(result)+"'");
						if (ndmf.get(name) != null) throw new AssertionError("New value mismatch! Expected:null Result:'"+getImageMD5(result)+"'");
					} else {
						checkResults.remove(name);
						ndmf.put(name, null);
					}
					
					break;
				}
				
				default:
			}
			
			if (CHECK_CONSISTENCY) lock.unlock();
		}
	}

	private static <T> T getRandomFromSet(Set<T> set, Random random) {
		final int size = set.size();
		if (size == 0) return null;
		
		final int index = random.nextInt(size);
		int currentIndex = 0;
		T result = null;
		for (T elem : set) {
			if (currentIndex == index) result = elem;
			currentIndex++;
		}
		return result;
	}

	private static String getImageMD5(BufferedImage image) {
		if (image == null) return "null";
		ByteArrayOutputStream baos = new ByteArrayOutputStream(image.getWidth()*image.getHeight()*4);
		try (DataOutputStream dos = new DataOutputStream(baos)) {
			int[] rgba = getImageRGBA(image);
			for (int i = 0; i < rgba.length; i++) {
				dos.writeInt(rgba[i]);
			}
			return "MD5:"+MD5Hash.hash(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "MD5:ERROR";
	}

	private static int[] getImageRGBA(BufferedImage image) {
		int[] rgba = new int[image.getWidth()*image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgba, 0, image.getWidth());
		return rgba;
	}
	
	private static String randomString(Random random, int length, boolean allowUppercase, boolean allowNums, boolean allowSymbols, boolean allowWeirdChars) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char chr = '\0';
			for (int k = 0; k < 20; k++) {
				chr = (char) (random.nextInt(256));
				if ("abcdefghijklmnopqrstuvwxyz".contains(""+chr)) break;
				if (allowUppercase && "ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(""+chr)) break;
				if (allowNums && Character.isDigit(chr)) break;
				if (allowSymbols && "!@#$%^&*()_+=-`~\\|[]{};:'\"/?.>,<".contains(""+chr)) break;
				if (allowWeirdChars && Character.isLetterOrDigit(chr)) break;
				else chr = '\0';
			}
			if (chr == '\0') chr = (char) ('a'+random.nextInt(26));
			sb.append(chr);
		}
		return sb.toString();
	}

//	private static void displayImage(String title, BufferedImage image) {
//		JFrame frame = new JFrame();
//		frame.setTitle(title);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.add(new JLabel(new ImageIcon(image)));
//		frame.pack();
//		frame.setVisible(true);
//	}

	private static boolean imageEqual(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth()) return false;
		if (a.getHeight() != b.getHeight()) return false;
		int[] rgbaA = getImageRGBA(a);
		int[] rgbaB = getImageRGBA(b);
		return Arrays.equals(rgbaA, rgbaB);
	}
	
	private static BufferedImage createImage(int width, int height, long seed) {
		Random random = new Random(seed);
		
		BufferedImage image1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image1.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, width, height);

		final int numStrs = 25;
		
		int size = Math.min(width, height);
		int fontSize = size/10;
		graphics.setFont(new Font("Courier New", Font.BOLD, fontSize));
		
		for (int i = 0; i < numStrs; i++) {
			float hue = random.nextFloat();
			float sat = 0.5f + 0.5f * random.nextFloat();
			float bri = (1.0f+i) / numStrs;
			Color colorLight = new Color(Color.HSBtoRGB(hue, sat, bri*0.4f+0.6f));
			Color colorDark = new Color(Color.HSBtoRGB(hue, sat, bri*0.2f));
			colorDark = new Color(colorDark.getRed(), colorDark.getGreen(), colorDark.getBlue(), 196);
			
			
			double x = random.nextDouble()*width;
			double y = random.nextDouble()*height;
			double r = (random.nextDouble()*2.0-1.0)*Math.PI*0.5;
			double s = 1.0/(1.0+0.04*(numStrs-i));
			
			AffineTransform reset = graphics.getTransform();
			graphics.translate(x, y);
			graphics.rotate(r);
			graphics.scale(s, s);


			String word = " "+randomString(random, 20, true, true, true, true)+" ";
			
			FontMetrics fontMetrics = graphics.getFontMetrics();
			int wordWidth = fontMetrics.stringWidth(word);
			Rectangle2D wordBounds = fontMetrics.getStringBounds(word, graphics);
			wordBounds = new Rectangle2D.Double(wordBounds.getMinX()-8, wordBounds.getMinY()-8, wordBounds.getWidth()+16, wordBounds.getHeight()+16);
			
			graphics.setColor(colorDark);
			graphics.fillRect((int) (-wordBounds.getWidth()/2), (int) wordBounds.getY(), (int) wordBounds.getWidth(), (int) wordBounds.getHeight());

			graphics.setColor(colorLight);
			graphics.drawString(word, -wordWidth/2, 0);
			
			graphics.setTransform(reset);
			
		}
		
		graphics.dispose();
		return image1;		
	}
	
}
