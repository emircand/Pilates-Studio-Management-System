import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { AthleteComponent } from './list/athlete.component';
import { AthleteDetailComponent } from './detail/athlete-detail.component';
import { AthleteUpdateComponent } from './update/athlete-update.component';
import AthleteResolve from './route/athlete-routing-resolve.service';

const athleteRoute: Routes = [
  {
    path: '',
    component: AthleteComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AthleteDetailComponent,
    resolve: {
      athlete: AthleteResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AthleteUpdateComponent,
    resolve: {
      athlete: AthleteResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AthleteUpdateComponent,
    resolve: {
      athlete: AthleteResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default athleteRoute;
