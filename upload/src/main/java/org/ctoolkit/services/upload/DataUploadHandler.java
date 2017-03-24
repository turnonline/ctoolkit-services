/*
 * Copyright (c) 2017 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.services.upload;

/**
 * Data upload handler constants.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface DataUploadHandler
{
    /**
     * The URL of the servlet to register in order to handle data upload.
     */
    String DATA_HANDLER_UPLOAD_URL = "/data-upload-handler";

    /**
     * The mandatory file marker.
     */
    String UPLOAD_NAME_FIELD_MARKER = "__name_field_marker";

    /**
     * Google cloud storage bucket name, if not used the default GCS bucket name (AppIdentity) will be used.
     */
    String PARAMETER_GCS_BUCKET_NAME = "__gcs_bucket_name";

    /**
     * The optional arbitrary custom name
     */
    String PARAMETER_CUSTOM_NAME = "__arb_custom_name";

    /**
     * The optional size of the image returned as serving URL. Valid sizes must be between 0 - 1600
     */
    String PARAMETER_IMAGE_SIZE = "__image_size";
}
