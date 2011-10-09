/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.service.artifact.upload;

import org.apache.commons.fileupload.ProgressListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by jfischer
 */
public class UploadProgressListener implements ProgressListener {

    private double percentDone;

    public UploadProgressListener(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        session.setAttribute(request.getParameter("callbackName"), this);
    }

    /*
    * (non-Javadoc)
    * @see org.apache.commons.fileupload.ProgressListener#update(long, long, int)
    */
    @Override
    public void update(long bytesRead, long contentLength, int pItems) {
        percentDone = (100 * bytesRead) / contentLength;
    }

    /**
     * Get the percent done
     *
     * @return the percent done
     */
    public double getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(double percentDone) {
        this.percentDone = percentDone;
    }

}
