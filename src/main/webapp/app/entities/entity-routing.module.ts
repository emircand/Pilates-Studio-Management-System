import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'staff',
        data: { pageTitle: 'pilatesapplicationApp.staff.home.title' },
        loadChildren: () => import('./staff/staff.routes'),
      },
      {
        path: 'qr-code',
        data: { pageTitle: 'pilatesapplicationApp.qRCode.home.title' },
        loadChildren: () => import('./qr-code/qr-code.routes'),
      },
      {
        path: 'session-package',
        data: { pageTitle: 'pilatesapplicationApp.sessionPackage.home.title' },
        loadChildren: () => import('./session-package/session-package.routes'),
      },
      {
        path: 'athlete',
        data: { pageTitle: 'pilatesapplicationApp.athlete.home.title' },
        loadChildren: () => import('./athlete/athlete.routes'),
      },
      {
        path: 'session',
        data: { pageTitle: 'pilatesapplicationApp.session.home.title' },
        loadChildren: () => import('./session/session.routes'),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
