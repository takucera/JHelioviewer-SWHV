package org.helioviewer.viewmodel.view.opengl.shader;

import javax.media.opengl.GL2;

import org.helioviewer.jhv.shaderfactory.ShaderFactory;
import org.helioviewer.viewmodel.view.opengl.shader.GLShaderBuilder;
import org.helioviewer.viewmodel.view.opengl.shader.GLShaderBuilder.GLBuildShaderException;
import org.helioviewer.viewmodel.view.opengl.shader.GLVertexShaderProgram;

public class GL3DImageVertexShaderProgram extends GLVertexShaderProgram {

    private double differenceXOffset;
    private double differenceYOffset;

    private double differenceXScale;
    private double differenceYScale;
    private final int rectRef = 0;
    private final int differenceRectRef = 6;
    private final int offsetRef = 5;

    private final int thetaRef = 1;
    private final int phiRef = 2;
    private final double[] thetaValue = new double[4];
    private final double[] phiValue = new double[4];
    private final int differenceThetaRef = 3;
    private final int differencePhiRef = 4;
    private final double[] differenceThetaValue = new double[4];
    private final double[] differencePhiValue = new double[4];

    /**
     * {@inheritDoc}
     */
    @Override
    public final void bind(GL2 gl) {
        if (shaderID != shaderCurrentlyUsed) {
            shaderCurrentlyUsed = shaderID;
            gl.glBindProgramARB(target, ShaderFactory.getVertexId());
            gl.glProgramLocalParameter4dARB(target, this.rectRef, xOffset, yOffset, xScale, yScale);
            gl.glProgramLocalParameter4dARB(target, this.offsetRef, defaultXOffset, defaultYOffset, 0, 0);
            gl.glProgramLocalParameter4dARB(target, this.differenceRectRef, differenceXOffset, differenceYOffset, differenceXScale, differenceYScale);
            this.bindEnvVars(gl, this.thetaRef, thetaValue);
            this.bindEnvVars(gl, this.phiRef, phiValue);
            this.bindEnvVars(gl, this.differenceThetaRef, differenceThetaValue);
            this.bindEnvVars(gl, this.differencePhiRef, differencePhiValue);
        }
    }

    private void bindEnvVars(GL2 gl, int id, double[] param) {
        gl.glProgramLocalParameter4dARB(target, id, param[0], param[1], param[2], param[3]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildImpl(GLShaderBuilder shaderBuilder) {
        try {
            String program = "";

            program += "\tif(abs(position.x)>1.1){" + GLShaderBuilder.LINE_SEP;
            //Corona

            program += "\toutput.x = position.x - rect.x;" + GLShaderBuilder.LINE_SEP;
            program += "\toutput.y = -position.y - rect.y;" + GLShaderBuilder.LINE_SEP;

            program += "\toutput.x *= rect.z;" + GLShaderBuilder.LINE_SEP;
            program += "\toutput.y *= rect.w;" + GLShaderBuilder.LINE_SEP;

            //Difference Image

            program += "\tdifferenceOutput.x = position.x - differenceRect.x;" + GLShaderBuilder.LINE_SEP;
            program += "\tdifferenceOutput.y = -position.y - differenceRect.y;" + GLShaderBuilder.LINE_SEP;

            program += "\tdifferenceOutput.x *= differenceRect.z;" + GLShaderBuilder.LINE_SEP;
            program += "\tdifferenceOutput.y *= differenceRect.w;" + GLShaderBuilder.LINE_SEP;

            program += "\tpositionPass = position;" + GLShaderBuilder.LINE_SEP;
            program += "\tfloat3x3 mat = float3x3(cos(phi), -sin(theta)*sin(phi), -sin(phi)*cos(theta), 0, cos(theta), -sin(theta), sin(phi), cos(phi)*sin(theta), cos(theta)*cos(phi));" + GLShaderBuilder.LINE_SEP;
            program += "\tphysicalPosition.xyz = mul(mat, physicalPosition.xyz);" + GLShaderBuilder.LINE_SEP;
            program += "\t OUT.position = mul(state_matrix_mvp, physicalPosition);" + GLShaderBuilder.LINE_SEP;
            program += "\t}" + GLShaderBuilder.LINE_SEP;
            program += "\telse{" + GLShaderBuilder.LINE_SEP;
            //Solar disk
            //Image

            program += "\tpositionPass = physicalPosition;" + GLShaderBuilder.LINE_SEP;
            program += "\tfloat3x3 mat = float3x3(cos(phi), 0, sin(phi), -sin(theta)*sin(phi), cos(theta), sin(theta)*cos(phi), -cos(theta)*sin(phi), -sin(theta), cos(theta)*cos(phi));" + GLShaderBuilder.LINE_SEP;
            program += "\tfloat3 rot = mul(mat, physicalPosition.xyz);" + GLShaderBuilder.LINE_SEP;

            program += "\toutput.x = rot.x - rect.x;" + GLShaderBuilder.LINE_SEP;
            program += "\toutput.y = -rot.y - rect.y;" + GLShaderBuilder.LINE_SEP;

            program += "\toutput.x *= rect.z;" + GLShaderBuilder.LINE_SEP;
            program += "\toutput.y *= rect.w;" + GLShaderBuilder.LINE_SEP;

            //Difference Image

            program += "\tmat = float3x3(cos(differencephi), 0, sin(differencephi), -sin(differencetheta)*sin(differencephi), cos(differencetheta), sin(differencetheta)*cos(differencephi), -cos(differencetheta)*sin(differencephi), -sin(differencetheta), cos(differencetheta)*cos(differencephi));" + GLShaderBuilder.LINE_SEP;
            program += "\trot = mul(mat, physicalPosition.xyz);" + GLShaderBuilder.LINE_SEP;

            program += "\tdifferenceOutput.x = rot.x - differenceRect.x;" + GLShaderBuilder.LINE_SEP;
            program += "\tdifferenceOutput.y = -rot.y - differenceRect.y;" + GLShaderBuilder.LINE_SEP;

            program += "\tdifferenceOutput.x *= differenceRect.z;" + GLShaderBuilder.LINE_SEP;
            program += "\tdifferenceOutput.y *= differenceRect.w;" + GLShaderBuilder.LINE_SEP;

            program += "}" + GLShaderBuilder.LINE_SEP;

            program = program.replace("output", shaderBuilder.useOutputValue("float4", "TEXCOORD0"));
            program = program.replace("physicalPosition", shaderBuilder.useStandardParameter("float4", "POSITION"));
            program = program.replace("differenceOutput", shaderBuilder.useOutputValue("float4", "TEXCOORD4"));

            program = program.replace("positionPass", shaderBuilder.useOutputValue("float4", "TEXCOORD3"));
            program = program.replace("color", shaderBuilder.useStandardParameter("float4", "COLOR"));
            shaderBuilder.addMainFragment(program);

            // System.out.println("VertexShader:\n" + shaderBuilder.getCode());
        } catch (GLBuildShaderException e) {
            e.printStackTrace();
        }
    }

    public void changeRect(double xOffset, double yOffset, double xScale, double yScale) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public void changeAngles(double theta, double phi) {
        this.thetaValue[0] = theta;
        this.phiValue[0] = phi;
    }

    public void changeDifferenceAngles(double theta, double phi) {
        this.differenceThetaValue[0] = theta;
        this.differencePhiValue[0] = phi;
    }

    public void setDifferenceRect(double differenceXOffset, double differenceYOffset, double differenceXScale, double differenceYScale) {
        this.differenceXOffset = differenceXOffset;
        this.differenceYOffset = differenceYOffset;
        this.differenceXScale = differenceXScale;
        this.differenceYScale = differenceYScale;
    }

}
