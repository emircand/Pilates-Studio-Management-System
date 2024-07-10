import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IQRCode } from '../qr-code.model';
import { QRCodeService } from '../service/qr-code.service';

export const qRCodeResolve = (route: ActivatedRouteSnapshot): Observable<null | IQRCode> => {
  const id = route.params['id'];
  if (id) {
    return inject(QRCodeService)
      .find(id)
      .pipe(
        mergeMap((qRCode: HttpResponse<IQRCode>) => {
          if (qRCode.body) {
            return of(qRCode.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default qRCodeResolve;
