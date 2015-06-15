/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author kwando
 */
public class RotationControl extends AbstractControl {

  private Vector3f rot;

  public RotationControl(Vector3f rot) {
    this.rot = rot;
  }

  @Override
  protected void controlUpdate(float tpf) {
    this.spatial.rotate(rot.x * tpf, rot.y * tpf, rot.z * tpf);
  }

  @Override
  protected void controlRender(RenderManager rm, ViewPort vp) {
  }

  public Control cloneForSpatial(Spatial spatial) {
    Control control = spatial.getControl(RotationControl.class);
    if (control == null) {
      control = new RotationControl(rot);
      spatial.addControl(control);
    }

    return control;
  }
}
