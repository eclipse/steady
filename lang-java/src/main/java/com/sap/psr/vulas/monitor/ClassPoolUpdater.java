package com.sap.psr.vulas.monitor;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.shared.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Updates the classpath of the Javassist {@link ClassPool}, which is needed to ensure that
 * instances of {@link CtClass} can be created. Example: Consideration of the declaring class in
 * nested class definitions, cf. {@link ClassVisitor#ClassVisitor(javassist.CtClass)}.
 */
public class ClassPoolUpdater {

  private static final Log log = LogFactory.getLog(ClassPoolUpdater.class);

  private static ClassPoolUpdater instance = null;

  /**
   * Created to contain the paths of the resources (JarFiles) loaded and analyzed be the
   * reachability analyzer
   */
  private ClassPool customClassPool = null;

  private Set<Path> appendedResources = null;
  private boolean useDefault = true;

  /** Constructor for ClassPoolUpdater. */
  public ClassPoolUpdater() {
    this(true);
  }

  private ClassPoolUpdater(boolean _use_default) {
    this.appendedResources = new HashSet<Path>();
    this.useDefault = _use_default;
    if (_use_default) this.customClassPool = ClassPool.getDefault();
    else this.customClassPool = new ClassPool();
  }

  /**
   * Returns an instance of {@link ClassPoolUpdater} making use of a custom Javassist {@link
   * ClassPool}. This instance will be created at the time of the first call, later calls will
   * return this instance. If the default {@link ClassPool} is sufficient, i.e., if no custom
   * resources need to be added to its classpath, one can also create an instance using the public
   * constructor.
   *
   * @return a {@link com.sap.psr.vulas.monitor.ClassPoolUpdater} object.
   */
  public static synchronized ClassPoolUpdater getInstance() {
    if (ClassPoolUpdater.instance == null) {
      ClassPoolUpdater.instance = new ClassPoolUpdater(false);
    }
    return ClassPoolUpdater.instance;
  }

  /**
   * getClasspaths.
   *
   * @param _files a {@link java.util.Set} object.
   * @return a {@link java.util.Set} object.
   */
  public Set<Path> getClasspaths(Set<Path> _files) {
    Set<Path> paths = new HashSet<Path>();
    Path path = null;
    for (Path file : _files) {
      if (file.toFile().getName().endsWith(".jar")) {
        paths.add(file);
      } else {
        path = this.getClasspath(file.toFile());
        if (path != null) paths.add(path);
      }
    }
    return paths;
  }

  /**
   * For a given class file, the method returns the directory ....
   *
   * @param _class_file a {@link java.io.File} object.
   * @return a {@link java.nio.file.Path} object.
   */
  public Path getClasspath(File _class_file) {
    // Must have file extension "class"
    if (!"class".equals(FileUtil.getFileExtension(_class_file)))
      throw new IllegalArgumentException(
          "Expected file with extension 'class', got [" + _class_file + "]");

    Path dir = null;
    Path p = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(_class_file);
      final CtClass ctclass = ClassPool.getDefault().makeClass(fis);
      p = _class_file.toPath();

      // Distinguish absolute and relative paths
      if (p.isAbsolute()) {
        dir =
            p.subpath(
                0, p.getNameCount() - 1); // Root is lost, as subpath always returns relative paths
        dir = p.getRoot().resolve(dir);
      } else {
        dir = p.subpath(0, p.getNameCount() - 1);
        dir = dir.toAbsolutePath().normalize();
      }

      final Path package_dir = this.getPackagePath(ctclass);

      // Class w/o package: Take dir as is
      if (package_dir == null) {
      }

      // Class w/ package, remove package related folders from dir
      else {

        // If the package dirs name is equal to the last name of pdir, remove the last name of pdir
        // Before while loop: pdir=/home/xyz/org/apache/x/y/z and package_dir=x/y/z
        // After while loop (if successful):  pdir=/home/xyz/
        // After while loop (if unsuccessful): pdir=null
        int current_idx = package_dir.getNameCount();
        Path name = null, root = dir.getRoot();
        while (current_idx-- > 0) {
          name = package_dir.getName(current_idx);
          if (dir.getName(dir.getNameCount() - 1).equals(name)) {
            dir = dir.subpath(0, dir.getNameCount() - 1);
          } else {
            dir = null;
            break;
          }
        }
        if (dir != null && root != null) dir = root.resolve(dir);
      }
    } catch (FileNotFoundException e) {
      ClassPoolUpdater.log.error("", e);
      dir = null;
    } catch (IOException e) {
      ClassPoolUpdater.log.error("", e);
      dir = null;
    } catch (RuntimeException e) {
      ClassPoolUpdater.log.error("", e);
      dir = null;
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          ClassPoolUpdater.log.error("Error closing input stream: " + e.getMessage(), e);
        }
      }
    }
    return dir;
  }

  /**
   * Appends a directory to the classpath of the default {@link ClassPool}. This directory
   * corresponds to the given file minus the path elements that correspond to the Java package
   * structure of the given {@link CtClass}. Returns true if the path has been appended, false
   * otherwise.
   *
   * @param _ctclass a {@link javassist.CtClass} object.
   * @param _file a {@link java.io.File} object.
   * @return a boolean.
   */
  public boolean updateClasspath(CtClass _ctclass, File _file) {
    // Append (null handled in other method)
    return this.appendToClasspath(this.getClasspath(_file));
  }

  /**
   * appendToClasspath.
   *
   * @param _paths a {@link java.util.Set} object.
   */
  public void appendToClasspath(Set<Path> _paths) {
    for (Path p : _paths) this.appendToClasspath(p);
  }

  /**
   * If not done already, appends the given {@link Path} to the classpath of the {@link
   * ClassPoolUpdater#customClassPool}.
   *
   * @param _path a {@link java.nio.file.Path} object.
   * @return a boolean.
   */
  public synchronized boolean appendToClasspath(Path _path) {
    boolean appended = false;
    if (_path != null) {
      final Path normalized = _path.normalize();
      if (!this.appendedResources.contains(normalized)) {
        try {
          this.customClassPool.appendClassPath(normalized.toString());
          this.appendedResources.add(normalized);
          appended = true;
          // ClassPoolUpdater.log.info("Appended [" + normalized + "] to the loadedResources class
          // pool");
        } catch (NotFoundException e) {
          ClassPoolUpdater.log.error(
              "Not found exception while appending ["
                  + normalized
                  + "] to the custom class pool: "
                  + e.getMessage(),
              e);
        } catch (Exception e) {
          ClassPoolUpdater.log.error(
              "Error while appending ["
                  + normalized
                  + "] to the custom class pool: "
                  + e.getMessage(),
              e);
        }
      }
    }
    return appended;
  }

  /**
   * countClasspathElements.
   *
   * @return a int.
   */
  public int countClasspathElements() {
    return this.appendedResources.size();
  }

  /**
   * Resets the {@link ClassPoolUpdater} to its initial state. In particular, a new instance of the
   * {@link ClassPool} will be created, and all all the {@link Path}s that have been added to its
   * class path will be removed.
   *
   * @see ClassPoolUpdater#appendToClasspath(Path)
   */
  public synchronized void reset() {
    this.appendedResources.clear();
    if (this.useDefault) this.customClassPool = ClassPool.getDefault();
    else this.customClassPool = new ClassPool();
  }

  /**
   * Returns a {@link ClassPool} object that can be populated using the methods {@link
   * #appendToClasspath(java.nio.file.Path)} and {@link #appendToClasspath(Set)}.
   *
   * @return A {@link ClassPool} object, can be null if there is none
   */
  public ClassPool getCustomClassPool() {
    return this.customClassPool;
  }

  /**
   * This method look into the custom ClassPool and return a string with the physical path of the
   * JAR if is present in the resources loaded into the ClassPool. If is not found it returns null
   * PS: We can add to the ClassPool all the resources that we want using the methods {@link
   * #appendToClasspath(java.nio.file.Path)} and {@link #appendToClasspath(Set)}.
   *
   * @param _cid the ConstructId that we want to search in the ClassPool
   * @return the JAR path or null if is not found
   */
  public URL getJarResourcePath(ConstructId _cid) {
    URL url = null;
    if (_cid instanceof JavaId) {
      final JavaId jid = (JavaId) _cid;
      if (jid.getType() == JavaId.Type.METHOD
          || jid.getType() == JavaId.Type.CONSTRUCTOR
          || jid.getType() == JavaId.Type.CLASSINIT) {
        url = this.customClassPool.find(_cid.getDefinitionContext().getQualifiedName());
      } else if (jid.getType() == JavaId.Type.CLASS
          || jid.getType() == JavaId.Type.INTERFACE
          || jid.getType() == JavaId.Type.ENUM
          || jid.getType() == JavaId.Type.NESTED_CLASS) {
        url = this.customClassPool.find(_cid.getQualifiedName());
      }
    }
    return url;
  }

  /**
   * Returns a path reflecting the package structure of the given {@link CtClass}.
   *
   * @param _ctclass
   * @return
   */
  private Path getPackagePath(CtClass _ctclass) {
    final String package_name = _ctclass.getPackageName();
    if (package_name == null) return null;
    else return Paths.get(package_name.replace('.', '/'));
  }
}
