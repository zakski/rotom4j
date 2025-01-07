> Task :module-app:com.szadowsz.rotom4j.app.Main.main()
Embedding GUI for Window: com.szadowsz.rotom4j.app.ProcessingRotom4J@4214deeb
  Loading shader C:\Code\pokemon\tools\other\rotom4j\.\data\shaders\guideGridPoints.glsl
  Selecting NCLR File
  Selected NCLR File: C:\Code\pokemon\srcs\changes\Gen 4\prologmon-masters\pm_dp_ose\src\contest\graphic\contest_obj\bigman.NCLR
  Loading File: C:\Code\pokemon\srcs\changes\Gen 4\prologmon-masters\pm_dp_ose\src\contest\graphic\contest_obj\bigman.NCLR

Generating default NCLR with 16 palettes and 256colours
  compressed=false
  compressed=false

NCLR file, bigman.NCLR, initialising with size of 90 bytes
  Supported File encoding: RLCN
  Bom: 0xfeff
  Version: 256
  Header Size: 10
  Number Of Sections: 2
  Reading NCLR file data
  PLTT Index: 16
  PCMP Index: 72
  Palette Magic: TTLP
  Palette Section Size: 56
  Palette Bit Depth: colors16
  Padding: 0
  Palette Length: 32
  Color Offset: 16
  Colors Per Palette: 16
  Total Number Of Colors: 16
  Registering GUI for NCLR File: bigman.NCLR
  Creating GUI for NCLR File: bigman.NCLR
  Initialising "Raw" Binary Editor
  Font Metrics Loaded for "Raw" Binary Editor
  Editor Row Position: Length: 2, Width 30.0, Height 154.0, YOffset 27.5
  Editor Data Content: Length: 16, Width 480.0, Height 154.0, XOffset 30.0, YOffset 27.5
  Editor Header Data: Width 480.0, Height 27.5, XOffset 30.0
  Created GUI for NCLR File: bigman.NCLR
  Loaded File: C:\Code\pokemon\srcs\changes\Gen 4\prologmon-masters\pm_dp_ose\src\contest\graphic\contest_obj\bigman.NCLR
  java.lang.NullPointerException: Cannot invoke "com.szadowsz.rotom4j.component.nitro.nclr.NCLRComponent.getPaletteNum()" because "this.display" is null
	at com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder.drawPreviewRect(NCLRFolder.java:57)
	at com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder.drawForeground(NCLRFolder.java:49)
	at com.szadowsz.gui.component.RComponent.draw(RComponent.java:603)
	at com.szadowsz.gui.window.pane.RWinBuffer.drawChildComponent(RWinBuffer.java:78)
	at com.szadowsz.gui.window.pane.RWinBuffer.drawChildren(RWinBuffer.java:136)
	at com.szadowsz.gui.window.pane.RWinBuffer.drawContent(RWinBuffer.java:172)
	at com.szadowsz.gui.window.pane.RWinBuffer.redrawIfNecessary(RWinBuffer.java:185)
	at com.szadowsz.gui.window.pane.RWinBuffer.draw(RWinBuffer.java:191)
	at com.szadowsz.gui.window.pane.RWindowPane.drawContent(RWindowPane.java:647)
	at com.szadowsz.gui.window.pane.RWindowPane.drawPane(RWindowPane.java:727)
	at com.szadowsz.gui.window.pane.RWindowPane.drawWindow(RWindowPane.java:914)
	at com.szadowsz.gui.window.RWindowManager.updateAndDrawWindows(RWindowManager.java:223)
	at com.szadowsz.gui.RotomGui.draw(RotomGui.java:378)
	at com.szadowsz.gui.RotomGui.draw(RotomGui.java:406)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at processing.core.PApplet$RegisteredMethods.handle(PApplet.java:1309)
	at processing.core.PApplet.handleMethods(PApplet.java:1456)
	at processing.core.PApplet.handleDraw(PApplet.java:2106)
	at processing.opengl.PSurfaceJOGL$DrawListener.display(PSurfaceJOGL.java:840)
	at jogamp.opengl.GLDrawableHelper.displayImpl(GLDrawableHelper.java:692)
	at jogamp.opengl.GLDrawableHelper.display(GLDrawableHelper.java:674)
	at jogamp.opengl.GLAutoDrawableBase$2.run(GLAutoDrawableBase.java:443)
	at jogamp.opengl.GLDrawableHelper.invokeGLImpl(GLDrawableHelper.java:1293)
	at jogamp.opengl.GLDrawableHelper.invokeGL(GLDrawableHelper.java:1147)
	at com.jogamp.newt.opengl.GLWindow.display(GLWindow.java:782)
	at com.jogamp.opengl.util.AWTAnimatorImpl.display(AWTAnimatorImpl.java:81)
	at com.jogamp.opengl.util.AnimatorBase.display(AnimatorBase.java:453)
	at com.jogamp.opengl.util.FPSAnimator$MainTask.run(FPSAnimator.java:178)
	at java.base/java.util.TimerThread.mainLoop(Timer.java:566)
	at java.base/java.util.TimerThread.run(Timer.java:516)
