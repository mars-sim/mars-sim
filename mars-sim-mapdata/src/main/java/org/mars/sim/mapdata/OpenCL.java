package org.mars.sim.mapdata;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenCL is a wrapper class for storing, finding and reusing programs
 * and kernels instead of constantly flushing and recreating contexts
 *
 * @see CLContext
 * @see CLProgram
 * @see CLCommandQueue
 */
public class OpenCL {
  private static Map<String, CLProgram> programs;
  private static Map<CLProgram, Map<String, CLKernel>> kernels;
  private static CLContext context;
  private static CLDevice device;
  private static CLCommandQueue queue;
  private static int localSize = -1;

  static {
    initCompute();
    Runtime.getRuntime().addShutdownHook(new Thread(OpenCL::destroy));
  }

  private static void initCompute() {
    programs = new HashMap<>();
    kernels = new HashMap<>();

    context = CLContext.create();
    device = context.getMaxFlopsDevice();
    queue = device.createCommandQueue();
  }

  /**
   * Retrieve a built program of the name provided using getSystemResourceAsStream
   * If the program was already loaded/built - load that instead
   * @param name the resource name
   * @return the requested cl program, built
   * @see ClassLoader#getSystemResourceAsStream(String)
   */
  public static CLProgram getProgram(String programName) {
    programs.computeIfAbsent(programName, k -> {
      InputStream stream = ClassLoader.getSystemResourceAsStream(programName);
      try {
        return context.createProgram(stream).build();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    return programs.get(programName);
  }

  /**
   * Retrieve a requested Kernel within a program, these are built and kept in memory until
   * destroy() is called.
   * @param program the program the kernel is attached to
   * @param kernelName the name of the kernel within the program
   * @return the requested CLKernel (rewound to 0)
   */
  public static CLKernel getKernel(CLProgram program, String kernelName) {
    kernels.computeIfAbsent(program, k -> new HashMap<>());
    kernels.get(program).computeIfAbsent(kernelName, k -> program.createCLKernel(kernelName));
    CLKernel result = kernels.get(program).get(kernelName);
    result.rewind();
    return result;
  }

  /**
   * This is currently not required, but may be in the future as a fallback to catch
   * Runtime Errors
   */
  public static void removeKernel(CLProgram program, String kernelName) {
    throw new UnsupportedOperationException();
  }

  /**
   * This is currently not required, but may be in the future as a fallback to catch
   * Runtime Errors
   */
  public static void removeProgram(String programName) {
    throw new UnsupportedOperationException();
  }

  public static int getGlobalSize(int size) {
    return getGlobalSize(size, getLocalSize());
  }

  private static int getGlobalSize(int size, int localWorkSize) {
    int globalSize = size;
    int r = globalSize % localWorkSize;
    if (r != 0) {
      globalSize += localWorkSize - r;
    }
    return globalSize;
  }

  public static int getLocalSize() {
    if(localSize < 0) {
      localSize = Math.min(device.getMaxWorkGroupSize(), 256);
    }
    return localSize;
  }

  /**
   * @return The current device in use.
   * @see CLContext
   */
  public static CLContext getContext() {
    return context;
  }

  /**
   * @return The current device in use.
   * @see CLDevice
   */
  public static CLDevice getDevice() {
    return device;
  }

  /**
   * @return The current queue in use.
   * @see CLCommandQueue
   */
  public static CLCommandQueue getQueue() {
    return queue;
  }

  /**
   * Clear, release and prepare for closure.
   * This must be called before the program shutdown to properly flush
   * the GPU and cached contexts.
   */
  public static void destroy() {
    kernels.clear();
    kernels = null;
    programs.clear();
    programs = null;
    if(context != null && !context.isReleased()) {
      context.release();
    }
  }
}
