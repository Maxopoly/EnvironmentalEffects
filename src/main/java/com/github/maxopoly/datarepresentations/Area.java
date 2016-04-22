package com.github.maxopoly.datarepresentations;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import com.github.maxopoly.exceptions.ConfigParseException;

/**
 * Used to describe an area for an effect, does not take y-level into account.
 * Additional shapes may be added to the shape enum if they are also implemented
 * in the isInRange method
 * 
 * @author Max
 *
 */
public class Area {
	public enum Shape {
		RECTANGLE, CIRCLE, BIOME, GLOBAL, RING;
	}

	private Biome biome;
	private Location center;
	private Shape shape;
	private int xSize;
	private int zSize;
	private int outerLimit;
	private int innerLimit;
	private int maximumY;
	private int minimumY;

	/**
	 * Needed for the chunk collecting of ringshapes
	 */
	private HashSet<Chunk> scannedChunks;

	/**
	 * Constructor for a ring shape
	 * 
	 * @param shape
	 *            must be Shape.Worldborder
	 * @param innerLimit
	 *            radius of the inner border of the ring
	 * @param outerLimit
	 *            radius of the outer border of the ring
	 * @param center
	 *            center of the ring
	 */
	public Area(Shape shape, int innerLimit, int outerLimit, Location center, int minimumY, int maximumY)
			throws ConfigParseException {
		if (shape != Shape.RING) {
			throw new ConfigParseException();
		}
		this.shape = shape;
		this.innerLimit = innerLimit;
		this.outerLimit = outerLimit;
		this.center = center;
		this.minimumY = minimumY;
		this.maximumY = maximumY;
	}

	/**
	 * Constructor for biome based areas
	 * 
	 * @param shape
	 *            must be Shape.BIOME
	 * @param biome
	 *            biome describing the area
	 * @throws ConfigParseException
	 *             In case this constructor is used with a shape other than
	 *             BIOME
	 */
	public Area(Shape shape, Biome biome) throws ConfigParseException {
		this.shape = shape;
		this.biome = biome;
		if (shape != Shape.BIOME) {
			throw new ConfigParseException();
		}
		this.minimumY = 0;
		this.maximumY = 255;
	}

	/**
	 * Constructor for global areas
	 * 
	 * @param shape
	 *            always Shape.GLOBAL
	 */
	public Area(Shape shape)
			throws ConfigParseException {
		if (shape != Shape.GLOBAL) {
			throw new ConfigParseException();
		}
		this.shape = shape;
		this.minimumY = 0;
		this.maximumY = 255;
	}

	/**
	 * Constructor for a geometrical shape whichs length and width are the same
	 * 
	 * @param shape
	 *            geometrical shape
	 * @param center
	 *            location on which the shape is centred
	 * @param size
	 *            half of the diameter of the shape
	 * @throws ConfigParseException
	 *             thrown if the given shape is a biome or global, those cant
	 *             have given sizes
	 */
	public Area(Shape shape, Location center, int size, int minimumY, int maximumY)
			throws ConfigParseException {
		this(shape, center, size, size,minimumY, maximumY);
	}

	/**
	 * Constructor for a geometrical shape whichs length and width are different
	 * 
	 * @param shape
	 *            geometrical shape
	 * @param center
	 *            location on which the shape is centred
	 * @param xSize
	 *            half of the diameter of the shape in x-direction/east-west
	 * @param zSize
	 *            half of the diameter of the shape in y-direction/north-south
	 * @throws ConfigParseException
	 *             thrown if this constructor is used with a global or biome
	 *             shape, because those cant have given sizes
	 */
	public Area(Shape shape, Location center, int xSize, int zSize, int minimumY, int maximumY)
			throws ConfigParseException {
		if (shape == Shape.BIOME || shape == Shape.GLOBAL) {
			throw new ConfigParseException();
		}
		this.shape = shape;
		this.center = center;
		this.xSize = xSize;
		this.zSize = zSize;
		this.minimumY = minimumY;
		this.maximumY = maximumY;
	}

	/**
	 * Checks whether the given location is inside this area
	 * 
	 * @param loc
	 *            location be compared to this area
	 * @return true if the location is inside the area, false if not
	 */
	public boolean isInArea(Location loc) {
		if (loc.getBlockY() > maximumY || loc.getBlockY() < minimumY) {
			return false;
		}
		switch (shape) {
		case GLOBAL:
			return true;
		case BIOME:
			return loc.getBlock().getBiome() == biome;
		case RECTANGLE:
			return xSize >= getXDifference(loc) && zSize >= getZDifference(loc);
		case CIRCLE:
			return getXZDistance(loc) <= xSize;
		case RING:
			double dis = getXZDistance(loc);
			return dis >= innerLimit && dis <= outerLimit;
		default:
			return false; // hopefully never gonna happen

		}
	}

	/**
	 * @return Whether this instance is global
	 */
	public boolean isGlobal() {
		return shape == Shape.GLOBAL;
	}

	/**
	 * Gets all the chunks in this area, only works for geometrical shapes.
	 * Depending on the size this might take a while, so use this method with
	 * caution
	 * 
	 * @return list of all chunks in the area
	 */
	public LinkedList<Chunk> getChunks() {
		scannedChunks = new HashSet<Chunk>();
		Chunk c;
		switch (shape) {
		case RING:
			c = center.getWorld().getChunkAt(
					new Location(center.getWorld(), center.getBlockX()
							+ (outerLimit + innerLimit) / 2, 1, center
							.getBlockZ() + (outerLimit + innerLimit) / 2));
			return getChunksRecursive(c);
		case RECTANGLE:
		case CIRCLE:
			return getChunksRecursive(center.getChunk());
		case BIOME:
			// no implementation, this method simply shouldnt be called with
			// biome
		case GLOBAL:
			// TODO worldborder integration to make this possible
		default:
			return null;

		}
	}

	/**
	 * Collects all chunks directly connected to the given chunk, by other
	 * chunks, which are all in the area this instance describes recursive
	 * 
	 * @param chunk
	 *            Chunk to start with
	 * @return List of all adjacent chunks in the area, which have a direction
	 *         connection to the given chunk
	 */
	private LinkedList<Chunk> getChunksRecursive(Chunk chunk) {
		if (scannedChunks.contains(chunk)) {
			return null;
		} else {
			scannedChunks.add(chunk);
		}
		if (!edgesInArea(chunk)) {
			return null;
		}
		LinkedList<Chunk> result = new LinkedList<Chunk>();
		LinkedList<Chunk> west = getChunksRecursive(chunk.getWorld()
				.getChunkAt(chunk.getX() - 1, chunk.getZ()));
		LinkedList<Chunk> east = getChunksRecursive(chunk.getWorld()
				.getChunkAt(chunk.getX() + 1, chunk.getZ()));
		LinkedList<Chunk> south = getChunksRecursive(chunk.getWorld()
				.getChunkAt(chunk.getX(), chunk.getZ() + 1));
		LinkedList<Chunk> north = getChunksRecursive(chunk.getWorld()
				.getChunkAt(chunk.getX(), chunk.getZ() - 1));
		if (west != null) {
			result.addAll(west);
		}
		if (east != null) {
			result.addAll(east);
		}
		if (south != null) {
			result.addAll(south);
		}
		if (north != null) {
			result.addAll(north);
		}
		return result;
	}

	private boolean edgesInArea(Chunk ch) {
		return isInArea(ch.getBlock(0, 0, 0).getLocation())
				|| isInArea(ch.getBlock(15, 0, 0).getLocation())
				|| isInArea(ch.getBlock(0, 0, 15).getLocation())
				|| isInArea(ch.getBlock(15, 0, 15).getLocation());
	}

	/**
	 * @return Shape of this area
	 */
	public Area.Shape getShape() {
		return shape;
	}

	/**
	 * @return center of this area
	 */
	public Location getCenter() {
		return center;
	}

	public int getxSize() {
		return xSize;
	}

	public int getzSize() {
		return zSize;
	}

	// these methods are not safe, calling them when the area is actually
	// not a geometrical shape with x and z will throw exceptions
	private double getXZDistance(Location loc) {
		double x = getXDifference(loc);
		double z = getZDifference(loc);
		return Math.sqrt(x * x + z * z);
	}

	private double getXDifference(Location loc) {
		return Math.abs(loc.getX() - center.getX());
	}

	private double getZDifference(Location loc) {
		return Math.abs(loc.getZ() - center.getZ());
	}
	
	public int getMaximumY() {
		return maximumY;
	}
	
	public int getMinimumY() {
		return minimumY;
	}

}
