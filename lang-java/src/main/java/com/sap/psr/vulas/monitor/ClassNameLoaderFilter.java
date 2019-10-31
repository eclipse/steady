package com.sap.psr.vulas.monitor;

/**
 * Checks whether the provided class loader is of the given type, or whether it is a child of
 * another class loader of that type. Use 'org.apache.catalina.loader.WebappClassLoader', for
 * instance, to only consider classes loaded/traced in the context of Web applications deployed in
 * Tomcat (but not classes loaded by parent class loaders, such as the system or bootstrap loader).
 */
public class ClassNameLoaderFilter implements LoaderFilter {

  private String classname = null;
  private boolean acceptChilds = false;

  /**
   * Constructor for ClassNameLoaderFilter.
   *
   * @param _classname a {@link java.lang.String} object.
   * @param _accept_childs a boolean.
   */
  public ClassNameLoaderFilter(String _classname, boolean _accept_childs) {
    this.classname = _classname;
    this.acceptChilds = _accept_childs;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true of the given _loader is accepted by the filter.
   */
  public boolean accept(Loader _loader) {
    if (_loader.getClassLoader().getClass().getName().equals(this.classname)) return true;

    if (!this.acceptChilds) return false;

    final Loader parent = _loader.getParent();
    if (parent == null) return false;
    else return this.accept(parent);
  }
}
