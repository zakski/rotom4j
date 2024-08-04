/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.nds4j.app.nodes.bin.core;


import com.szadowsz.nds4j.file.bin.core.BinaryData;

/**
 * Data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface DataProvider {

    /**
     * Returns data or null.
     *
     * @return binary data
     */
    BinaryData getContentData();

    /**
     * Returns size of data or 0 if no data is present.
     *
     * @return size of data
     */
    long getDataSize();
}
