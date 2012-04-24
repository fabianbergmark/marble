/**
 * Copyright (c) 2008-2011 Ardor Labs, Inc.
 * 
 * This file is part of Ardor3D.
 * 
 * Ardor3D is free software: you can redistribute it and/or modify it under the
 * terms of its license which may be found in the accompanying LICENSE file or
 * at <http://www.ardor3d.com/LICENSE>.
 */

package org.marble.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Bend extends Mesh {

    protected int _circleSamples;

    protected int _radialSamples;

    protected final float _angle;

    protected final float _width;

    protected final boolean _spiral;

    protected final float _radius;

    /**
     * Constructs a new Torus. Center is the origin, but the Torus may be
     * transformed.
     * 
     * @param circleSamples
     *            The number of samples along the circles.
     * @param radialSamples
     *            The number of samples along the radial.
     * @param tubeRadius
     *            the radius of the torus tube.
     * @param centerRadius
     *            The distance from the center of the torus hole to the center
     *            of the torus tube.
     */
    public Bend(final int circleSamples, final int radialSamples,
            final float radius, final float width, final float angle,
            final boolean spiral) {
        _circleSamples = circleSamples;
        _radialSamples = radialSamples;
        _radius = radius;
        _width = width;
        _angle = angle;
        _spiral = spiral;

        setGeometryData();
        setIndexData();

    }

    private void setGeometryData() {
        // allocate vertices
        final int verts = ((_circleSamples + 1) * (_radialSamples + 1));
        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(verts);

        // allocate normals if requested
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(verts);

        // allocate texture coordinates
        final FloatBuffer texcoordBuffer =
                BufferUtils.createVector2Buffer(verts);

        // generate geometry
        final float inverseCircleSamples = 1.0f / _circleSamples;
        final float inverseRadialSamples = 1.0f / _radialSamples;
        int i = 0;
        // generate the cylinder itself
        final Vector3f radialAxis = new Vector3f(), torusMiddle =
                new Vector3f(), tempNormal = new Vector3f();
        for (int circleCount = 0; circleCount < _circleSamples; circleCount++) {
            // compute center point on torus circle at specified angle
            final float circleFraction = circleCount * inverseCircleSamples;
            final float theta = _angle * circleFraction;
            final float cosTheta = FastMath.cos(theta);
            final float sinTheta = FastMath.sin(theta);
            radialAxis.set(cosTheta, sinTheta, 0);
            radialAxis.mult(_radius, torusMiddle);
            if (_spiral) {
                torusMiddle.setZ(theta);
            }

            // compute slice vertices with duplication at end point
            final int iSave = i;
            for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
                final float radialFraction = radialCount * inverseRadialSamples;
                // in [0,1)
                final float phi = FastMath.TWO_PI * radialFraction;
                final float cosPhi = FastMath.cos(phi);
                final float sinPhi = FastMath.sin(phi);
                tempNormal.set(radialAxis).multLocal(cosPhi);
                tempNormal.setZ(tempNormal.getZ() + sinPhi);
                tempNormal.normalizeLocal();

                normalBuffer.put(tempNormal.getX()).put(tempNormal.getY())
                        .put(tempNormal.getZ());

                tempNormal.multLocal(_width).addLocal(torusMiddle);
                vertexBuffer.put(tempNormal.getX()).put(tempNormal.getY())
                        .put(tempNormal.getZ());

                texcoordBuffer.put(radialFraction).put(circleFraction);
                i++;
            }

            BufferUtils.copyInternalVector3(vertexBuffer, iSave, i);
            BufferUtils.copyInternalVector3(normalBuffer, iSave, i);

            texcoordBuffer.put(1.0f).put(circleFraction);

            i++;
        }

        // duplicate the cylinder ends to form a torus
        for (int iR = 0; iR <= _radialSamples; iR++, i++) {
            BufferUtils.copyInternalVector3(vertexBuffer, iR, i);
            BufferUtils.copyInternalVector3(normalBuffer, iR, i);
            BufferUtils.copyInternalVector2(texcoordBuffer, iR, i);
            texcoordBuffer.put(i * 2 + 1, 1.0f);
        }

        setBuffer(Type.Position, 3, vertexBuffer);
        setBuffer(Type.Normal, 3, normalBuffer);
        setBuffer(Type.TexCoord, 2, texcoordBuffer);
    }

    private void setIndexData() {
        final int tris = (2 * _circleSamples * _radialSamples);
        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(3 * tris);
        int i;
        // generate connectivity
        int connectionStart = 0;
        for (int circleCount = 0; circleCount < _circleSamples - 1; circleCount++) {
            int i0 = connectionStart;
            int i1 = i0 + 1;
            connectionStart += _radialSamples + 1;
            int i2 = connectionStart;
            int i3 = i2 + 1;
            for (i = 0; i < _radialSamples; i++) {

                indexBuffer.put(i0++);
                indexBuffer.put(i2);
                indexBuffer.put(i1);
                indexBuffer.put(i1++);
                indexBuffer.put(i2++);
                indexBuffer.put(i3++);

            }
        }
        setBuffer(Type.Index, 3, indexBuffer);
    }

}