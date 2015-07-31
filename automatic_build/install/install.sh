###############################################################################
# Copyright (c) 2000-2014 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
eclipse_sdk=/home/titanrt/eclipse_sdk/eclipse-SDK-4.2-linux-gtk-x86_64
SDK_NAME=`basename ${eclipse_sdk}`

rm -rf ${SDK_NAME}
cp -r ${eclipse_sdk} ./
cd ${SDK_NAME}
eclipse \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -repository http://antlreclipse.sourceforge.net/updates,file://proj/TTCN/www/ttcn/root/download/experimental_update_site/\
  -installIU org.antlr.ui.feature.group,TITAN_Log_Viewer.feature.group, TITAN_Designer.feature.group, TITAN_Executor.feature.group \
  -destination `pwd` \
  -profile SDKProfile
