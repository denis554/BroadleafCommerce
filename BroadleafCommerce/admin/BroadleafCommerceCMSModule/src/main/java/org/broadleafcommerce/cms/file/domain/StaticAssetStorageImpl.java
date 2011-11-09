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

package org.broadleafcommerce.cms.file.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.sql.Blob;

/**
 * Created by jfischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_STATIC_ASSET_STRG")
public class StaticAssetStorageImpl implements StaticAssetStorage {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StaticAssetStorageId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "StaticAssetStorageId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "StaticAssetStorageImpl", allocationSize = 10)
    @Column(name = "STATIC_ASSET_STRG_ID")
    protected Long id;

    @Column(name ="STATIC_ASSET_ID", nullable = false)
    protected Long staticAssetId;

    @Column (name = "FILE_DATA")
    @Lob
    protected Blob fileData;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Blob getFileData() {
        return fileData;
    }

    @Override
    public void setFileData(Blob fileData) {
        this.fileData = fileData;
    }

    @Override
    public Long getStaticAssetId() {
        return staticAssetId;
    }

    @Override
    public void setStaticAssetId(Long staticAssetId) {
        this.staticAssetId = staticAssetId;
    }
}
