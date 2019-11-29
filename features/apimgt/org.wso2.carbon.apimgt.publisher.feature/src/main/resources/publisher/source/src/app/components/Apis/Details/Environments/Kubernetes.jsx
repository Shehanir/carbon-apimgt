/*

 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import Checkbox from '@material-ui/core/Checkbox';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    mainTitle: {
        paddingTop: theme.spacing(3),
    },
    gatewayPaper: {
        marginTop: theme.spacing(2),
    },
    content: {
        marginTop: theme.spacing(2),
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
    },
}));

/**
 * Renders CloudClustersInfo List
 * @class CloudClusters
 * @param {*} props
 * @extends {React.Component}
 */
export default function CloudClusters(props) {
    const classes = useStyles();
    const { cloudClustersInfo } = props;
    console.log('info in Kubernetes component');
    console.log(cloudClustersInfo);

    return (
        <>
            <Typography variant='h4' align='left' className={classes.mainTitle}>
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.CloudClusters'
                    defaultMessage={cloudClustersInfo.name}
                />
            </Typography>

            <Paper className={classes.saveButton}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell />
                            <TableCell align='left'>Name</TableCell>
                            <TableCell align='left'>Namespace</TableCell>
                            <TableCell align='left'>Master URL</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow key={cloudClustersInfo.name}>
                            <TableCell padding='checkbox'>
                                <Checkbox
                                    // disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                    checked={console.log('checked ' + cloudClustersInfo.name)}
                                    onChange={
                                        (event) => {
                                            const { checked, name } = event.target;
                                            console.log('checkbox');
                                            console.log(checked + ' ' + name);
                                            // if (checked) {
                                            //     setGatewayEnvironments([...gatewayEnvironments, name]);
                                            // } else {
                                            //     setGatewayEnvironments(
                                            //         gatewayEnvironments.filter((env) => env !== name),
                                            //     );
                                            // }
                                        }
                                    }
                                    name={cloudClustersInfo.name}
                                    color='primary'
                                />
                            </TableCell>
                            <TableCell component='th' scope='row'>
                                {cloudClustersInfo.name}
                            </TableCell>
                            <TableCell align='left'>{cloudClustersInfo.namespace}</TableCell>
                            <TableCell align='left'>{cloudClustersInfo.masterUrl}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </Paper>
        </>
    );
}
