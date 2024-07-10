import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { QRCodeComponent } from './list/qr-code.component';
import { QRCodeDetailComponent } from './detail/qr-code-detail.component';
import { QRCodeUpdateComponent } from './update/qr-code-update.component';
import QRCodeResolve from './route/qr-code-routing-resolve.service';

const qRCodeRoute: Routes = [
  {
    path: '',
    component: QRCodeComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: QRCodeDetailComponent,
    resolve: {
      qRCode: QRCodeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: QRCodeUpdateComponent,
    resolve: {
      qRCode: QRCodeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: QRCodeUpdateComponent,
    resolve: {
      qRCode: QRCodeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default qRCodeRoute;
