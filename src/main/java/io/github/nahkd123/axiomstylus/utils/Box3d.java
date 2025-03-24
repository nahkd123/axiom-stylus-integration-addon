package io.github.nahkd123.axiomstylus.utils;

import java.util.function.Function;
import java.util.stream.Stream;

import org.joml.Matrix4dc;
import org.joml.Vector4d;

import net.minecraft.util.math.Vec3d;

public record Box3d(double x, double y, double z, double sizeX, double sizeY, double sizeZ) {
	public static Box3d radius(double rx, double ry, double rz) {
		return new Box3d(-rx, -ry, -rz, rx * 2, ry * 2, rz * 2);
	}

	public static Box3d enclosing(Box3d... boxes) {
		if (boxes.length == 0) return null;
		if (boxes.length == 1) return boxes[0];

		double minX = boxes[0].x;
		double minY = boxes[0].y;
		double minZ = boxes[0].z;
		double maxX = boxes[0].x + boxes[0].sizeX;
		double maxY = boxes[0].y + boxes[0].sizeY;
		double maxZ = boxes[0].z + boxes[0].sizeZ;

		for (int i = 1; i < boxes.length; i++) {
			minX = Math.min(minX, boxes[i].x);
			minY = Math.min(minY, boxes[i].y);
			minZ = Math.min(minZ, boxes[i].z);
			maxX = Math.max(maxX, boxes[i].x + boxes[i].sizeX);
			maxY = Math.max(maxY, boxes[i].y + boxes[i].sizeY);
			maxZ = Math.max(maxZ, boxes[i].z + boxes[i].sizeZ);
		}

		return new Box3d(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ);
	}

	public static Box3d enclosing(Vec3d... vertices) {
		if (vertices.length == 0) return null;
		if (vertices.length == 1) return new Box3d(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0, 0);

		double minX = vertices[0].x, minY = vertices[0].y, minZ = vertices[0].z;
		double maxX = minX, maxY = minY, maxZ = minZ;

		for (int i = 1; i < vertices.length; i++) {
			minX = Math.min(minX, vertices[i].x);
			minY = Math.min(minY, vertices[i].y);
			minZ = Math.min(minZ, vertices[i].z);
			maxX = Math.max(maxX, vertices[i].x);
			maxY = Math.max(maxY, vertices[i].y);
			maxZ = Math.max(maxZ, vertices[i].z);
		}

		return new Box3d(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ);
	}

	public Vec3d[] copyOfVertices() {
		return new Vec3d[] {
			new Vec3d(x, y, z),
			new Vec3d(x + sizeX, y, z),
			new Vec3d(x, y, z + sizeZ),
			new Vec3d(x + sizeX, y, z + sizeZ),
			new Vec3d(x, y + sizeY, z),
			new Vec3d(x + sizeX, y + sizeY, z),
			new Vec3d(x, y + sizeY, z + sizeZ),
			new Vec3d(x + sizeX, y + sizeY, z + sizeZ),
		};
	}

	public Box3d transformVerticesAndEnclose(Function<Vec3d, Vec3d> transformer) {
		Vec3d[] vertices = copyOfVertices();
		for (int i = 0; i < vertices.length; i++) vertices[i] = transformer.apply(vertices[i]);
		return Box3d.enclosing(vertices);
	}

	public Box3d tranformVerticesAndEnclose(Matrix4dc mat4, double w) {
		return enclosing(Stream.of(copyOfVertices())
			.map(v -> mat4.transform(v.x, v.y, v.z, w, new Vector4d()))
			.map(v -> new Vec3d(v.x, v.y, v.z))
			.toArray(Vec3d[]::new));
	}
}
