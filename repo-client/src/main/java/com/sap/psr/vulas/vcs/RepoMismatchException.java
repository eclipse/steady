package com.sap.psr.vulas.vcs;

import java.net.URL;


/**
 * Thrown to indicate that a given IVCSClient cannot interact with a VCS repository at the given URL.
 * Reasons can be, for instance, that there is no VCS repository at all or a different type than expected (e.g., GIT instead of SVN).
 * Connectivity problems should NOT be indicated using this exception.
 *
 */
public class RepoMismatchException extends Exception {
	public RepoMismatchException(IVCSClient _client, String _url, Throwable _cause) {
		super("VCS client " + ((Object)_client).getClass().getName() + " (type " +  _client.getType() + ") does not match to the repository (if any) at URL " + _url, _cause);
	}
}
