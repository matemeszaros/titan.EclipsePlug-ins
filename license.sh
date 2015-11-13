###############################################################################
# Copyright (c) 2000-2015 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
# switch on sending of usage statistics in titan eclipse
perl -i -p -e 's/(^[^\S\n]*public static final boolean USAGE_STAT_SENDING = )(false|true);/\1true;/g' org.eclipse.titan.common/src/org/eclipse/titan/common/product/ProductConstants.java 

# switch on license file checking in titan eclipse
perl -i -p -e 's/(^[^\S\n]*public static final boolean LICENSE_NEEDED = )(false|true);/\1true;/g' org.eclipse.titan.common/src/org/eclipse/titan/common/product/ProductConstants.java 
